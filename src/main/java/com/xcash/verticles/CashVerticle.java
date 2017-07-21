package com.xcash.verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
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
import com.xcash.entity.CashTransaction;
import com.xcash.entity.Channel;
import com.xcash.entity.TransactionCode;
import com.xcash.service.CashService;
import com.xcash.service.CashServiceJuZhenImpl;
import com.xcash.util.Runner;

public class CashVerticle extends AbstractVerticle {

	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);

	private static final String HOST = "0.0.0.0";
	private static final int PORT = 8082;
	private CashService service;
	private CashTransactionDAO dao;
	
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
		
		service = new CashServiceJuZhenImpl(vertx);
		JsonObject dbConfig = new JsonObject().put("host", "127.0.0.1").put("port", 5432).put("maxPoolSize",10).put("username", "xpay").put("password", "").put("database", "xpay");
		this.dao = new CashTransactionDAO(vertx, dbConfig);

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
		dao.insert(transaction, dbRes -> {
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
		dao.findByOrderNo(transaction.getOrderId(), trans -> {
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

		dao.updateStatus(orderId, CashTransaction.SUCCESS, dbRes -> {
			JsonObject response = new JsonObject().put("respCode", "00000").put("respInfo",  "OK");
			success(context, response);
		});
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
		context.response().setStatusCode(500).end();
	}

	private void badRequest(RoutingContext context) {
	    context.response().setStatusCode(400).end();
	}
	  
	private void success(RoutingContext context, JsonObject response) {
	  context.response().putHeader("content-type", "application/json").end(response.encodePrettily());
	}
}
