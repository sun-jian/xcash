package com.xcash.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xcash.dao.CashTransactionDAO;
import com.xcash.dao.OrderDAO;
import com.xcash.entity.CashTransaction;
import com.xcash.entity.Channel;
import com.xcash.entity.Order;
import com.xcash.entity.TransactionCode;
import com.xcash.service.CashService;
import com.xcash.service.CashServiceJuZhenImpl;
import com.xcash.service.XpayService;
import com.xcash.util.Runner;

public class CashVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);

	private static final String HOST = "0.0.0.0";
	private static final int PORT = 8082;
	private CashService service;
	private CashTransactionDAO cashDao;
	private OrderDAO orderDao;
	private XpayService xpayService;
	
	  // Convenience method so you can run it in your IDE
	  public static void main(String[] args) {
	    Runner.runExample(CashVerticle.class);
	  }

	
	@Override
	public void start(Future<Void> future) throws Exception {
		Router router = Router.router(vertx);
		// CORS support
		Set<String> allowHeaders = new HashSet<>();
		allowHeaders.add("x-requested-with");
		allowHeaders.add("Access-Control-Allow-Origin");
		allowHeaders.add("origin");
		allowHeaders.add("Content-Type");
		allowHeaders.add("accept");
		Set<HttpMethod> allowMethods = new HashSet<>();
		allowMethods.add(HttpMethod.GET);
		allowMethods.add(HttpMethod.POST);
		allowMethods.add(HttpMethod.DELETE);
		allowMethods.add(HttpMethod.PATCH);

		router.route().handler(BodyHandler.create());
		router.route().handler(CorsHandler.create("*").allowedHeaders(allowHeaders).allowedMethods(allowMethods));

		// routes
		router.post("/xcash/v1/cashing").handler(this::handlePostCash);
		router.post("/xcash/v1/query").handler(this::handleQuery);
		router.post("/xcash/v1/balance").handler(this::handleBalance);
		router.post("/xcash/notify/:channel").handler(this::handleNotify);
		router.get("/xcash/order").handler(this::handleQueryOrder);
		router.delete("/xcash/refund").handler(this::handleRefund);
		
		service = new CashServiceJuZhenImpl(vertx);
		xpayService = new XpayService(vertx);
		JsonObject dbConfig = new JsonObject().put("host", "127.0.0.1").put("port", 5432).put("maxPoolSize",10).put("username", "xpay").put("password", "").put("database", "xpay");
		SQLClient sqlClient = PostgreSQLClient.createShared(vertx, dbConfig, "xcash");
		this.cashDao = new CashTransactionDAO(vertx, sqlClient);
		this.orderDao = new OrderDAO(vertx, sqlClient);
		vertx.createHttpServer()
				.requestHandler(router::accept)
				.listen(config().getInteger("http.port", PORT),
						config().getString("http.address", HOST), result -> {
							if (result.succeeded())
								future.complete();
							else
								future.fail(result.cause());
						});
	}

	private void handlePostCash(RoutingContext context) {
		JsonObject body = context.getBodyAsJson();
		if (body == null) {
			  badRequest(context);
		      return;
		}
		CashTransaction transaction = new CashTransaction(body);
		transaction.setTc(TransactionCode.CASHING);
		if (!transaction.isValid()) {
			  badRequest(context);
		      return;
		}
		cashDao.insert(transaction, dbRes -> {
			service.postCash(transaction, res -> {
				handleResponse(context, res);
			 });
		});
	 }

	
	private void handleQuery(RoutingContext context) {
		JsonObject body = context.getBodyAsJson();
		if (body == null) {
			badRequest(context);
		    return;
		}
		CashTransaction transaction = new CashTransaction(body);
		transaction.setTc(TransactionCode.QUERY);
		if (!transaction.isValid()) {
			badRequest(context);
		    return;
		}
		cashDao.findByOrderNo(transaction.getOrderId(), trans -> {
			JsonObject tranJson = trans.result();
			boolean found = tranJson.getBoolean("found");
			String status = tranJson.getString("status");
			if(!found) {
				notFound(context);
				return;
			} 
			if(!CashTransaction.SUCCESS.equals(status)) {
				service.query(transaction, res -> {
					if (res==null)
					    notFound(context);
					else if(res.succeeded()) {
						String extStatus = res.result().getString("status");
						String extError = res.result().getString("error");
						tranJson.put("status", extStatus);
						if(StringUtils.isNotBlank(extError)) {
							tranJson.put("error", extError);
						}
						tranJson.remove("found");
						success(context, tranJson);
					} else {
						serverError(context, res.cause());
					}
				 });
			} else {
				success(context, tranJson);
			}
		});
	 }
	 
	private void handleBalance(RoutingContext context) {
		JsonObject body = context.getBodyAsJson();
		if (body == null) {
			badRequest(context);
		    return;
		}
		CashTransaction transaction = new CashTransaction(body);
		transaction.setTc(TransactionCode.BALANCE);
		if (!transaction.isValid()) {
			badRequest(context);
		    return;
		}
		service.balance(transaction, res -> {
			handleResponse(context, res);
		 });
	 }
	
	private void handleNotify(RoutingContext context) {
		JsonObject body = context.getBodyAsJson();
		if (body == null) {
			badRequest(context);
		    return;
		}
		Optional<Channel> channel = Channel.fromValue(context.request().getParam("channel"));
		if (!channel.isPresent()) {
			badRequest(context);
		    return;
		}
		String orderId = body.getString("orderId");
		if(StringUtils.isBlank(orderId)) {
			badRequest(context);
		    return;
		}
		LOGGER.info("Notify from {} : {}", channel.get().getId(), body);

		cashDao.updateStatus(orderId, CashTransaction.SUCCESS, dbRes -> {
			JsonObject response = new JsonObject().put("respCode", "00000").put("respInfo",  "OK");
			success(context, response);
		});
	}
	
	private void handleQueryOrder(RoutingContext context) {
		String orderNo = context.request().getParam("orderNo");
		String sellerOrderNo = context.request().getParam("sellerOrderNo");
		String extOrderNo = context.request().getParam("extOrderNo");
		String targetOrderNo = context.request().getParam("targetOrderNo");
		if(StringUtils.isBlank(orderNo) && StringUtils.isBlank(sellerOrderNo) && StringUtils.isBlank(extOrderNo) && StringUtils.isBlank(targetOrderNo)) {
			badRequest(context);
		    return;
		}
		Handler<AsyncResult<Optional<Order>>> resultHandler = dbRes -> {
			if(dbRes.succeeded() && dbRes.result().isPresent()) {
				Order order = dbRes.result().get();
				
				Future<Void> fut1 = Future.future();
				orderDao.findAppId(order.getAppId(), appKey -> {
					order.setAppKey(appKey.result());
					fut1.complete();
				});
				
				Future<Void> fut2 = Future.future();
				orderDao.findStoreById(order.getStoreId(), store -> {
					order.setStoreCode(store.result().getString("code"));
					order.setStoreName(store.result().getString("name"));
					fut2.complete();
				});
				
				Future<Void> fut3 = Future.future();
				orderDao.findChannelById(order.getChannelId(), channel -> {
					order.setExtStoreId(channel.result().getString("extStoreId"));
					order.setPaymentGateway(channel.result().getString("paymentGateway"));
					fut3.complete();
				});
				
				CompositeFuture.join(fut1,fut2, fut3).setHandler( ar -> {
					if (ar.succeeded()) {
						JsonObject jsonObject = new JsonObject(Json.encode(order));
						success(context,jsonObject);
					} else {
						serverError(context, ar.cause());
					}
				});
			} else {
				serverError(context, dbRes.cause());
			}
		};
		
		if(StringUtils.isNotBlank(orderNo)) {
			orderDao.findByOrderNo(orderNo, resultHandler);
		} else if(StringUtils.isNotBlank(sellerOrderNo)) {
			orderDao.findBySellerOrderNo(sellerOrderNo, resultHandler);
		} else if(StringUtils.isNotBlank(extOrderNo)) {
			orderDao.findByExtOrderNo(extOrderNo, resultHandler);
		} else if(StringUtils.isNotBlank(targetOrderNo)) {
			orderDao.findByTargetOrderNo(targetOrderNo, resultHandler);
		}
	}
	
	private void handleRefund(RoutingContext context) {
		String orderNo = context.request().getParam("orderNo");
		String sellerOrderNo = context.request().getParam("sellerOrderNo");
		String extOrderNo = context.request().getParam("extOrderNo");
		String targetOrderNo = context.request().getParam("targetOrderNo");
		if(StringUtils.isBlank(orderNo) && StringUtils.isBlank(sellerOrderNo) && StringUtils.isBlank(extOrderNo) && StringUtils.isBlank(targetOrderNo)) {
			badRequest(context);
		    return;
		}
		Handler<AsyncResult<Optional<Order>>> resultHandler = dbRes -> {
			if(dbRes.succeeded() && dbRes.result().isPresent()) {
				Order order = dbRes.result().get();
				
				Future<Void> fut1 = Future.future();
				orderDao.findAppId(order.getAppId(), appKey -> {
					order.setAppKey(appKey.result());
					fut1.complete();
				});
				
				Future<Void> fut2 = Future.future();
				orderDao.findStoreById(order.getStoreId(), store -> {
					order.setStoreCode(store.result().getString("code"));
					order.setStoreName(store.result().getString("name"));
					fut2.complete();
				});
				CompositeFuture.join(fut1,fut2).setHandler( ar -> {
					if (ar.succeeded()) {
						xpayService.refund(order, rf -> {
							if (rf.succeeded() && rf.result().getInteger("orderStatus", 0) == 1) {
								order.setStatus("REFUND");
								JsonObject jsonObject = new JsonObject(Json.encode(order));
								success(context,jsonObject);
							} else {
								if(rf.result()!=null) {
									serverError(context, rf.result().getString("message"));
								} else {
									serverError(context, rf.cause());
								}
							}
							
						});
					} else {
						serverError(context, ar.cause());
					}
				});
			} else {
				serverError(context, dbRes.cause());
			}
		};
		
		if(StringUtils.isNotBlank(orderNo)) {
			orderDao.findByOrderNo(orderNo, resultHandler);
		} else if(StringUtils.isNotBlank(sellerOrderNo)) {
			orderDao.findBySellerOrderNo(sellerOrderNo, resultHandler);
		} else if(StringUtils.isNotBlank(extOrderNo)) {
			orderDao.findByExtOrderNo(extOrderNo, resultHandler);
		} else if(StringUtils.isNotBlank(targetOrderNo)) {
			orderDao.findByTargetOrderNo(targetOrderNo, resultHandler);
		}
	 }


	private void handleResponse(RoutingContext context,
			AsyncResult<JsonObject> res) {
		if (res==null)
		   notFound(context);
		else {
			if(res.succeeded()) {
				success(context, res.result());
			} else {
				serverError(context, res.cause());
			}
		}
	}
	
	private void notFound(RoutingContext context) {
		context.response().setStatusCode(404).end();
	}
	  
	private void serverError(RoutingContext context, Throwable cause) {
		String message = cause == null? "Order not found":cause.getMessage();
		context.response().setStatusCode(500).end(message);
	}

	private void serverError(RoutingContext context, String message) {
		context.response().setStatusCode(500).end(message);
	}
	
	private void badRequest(RoutingContext context) {
	    context.response().setStatusCode(400).end();
	}
	  
	private void success(RoutingContext context, JsonObject response) {
	  context.response().putHeader("content-type", "application/json").end(response.encodePrettily());
	}
}
