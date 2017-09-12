package com.xcash.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xcash.entity.Order;
import com.xcash.verticles.CashVerticle;

public class XpayService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);
	private static final String TOKEN_PATH="http://106.14.47.193/xpay/tokens/{appKey}";
	private static final String REFUND_PATH="http://106.14.47.193/xpay/rest/v1/pay/refund/{orderNo}?storeId={storeId}&isCsr=true";
	private static final String UNIFIER_ORDER_PATH="http://106.14.47.193/xpay/rest/v1/pay/unifiedorder?storeId={storeId}&payChannel=2&totalFee=0.01&orderTime=20170930160618&sellerOrderNo=1504080378&attach=a&ip=127.0.0.1&returnUrl=http://www.baidu.com";
	
	private final Vertx vertx;

	public XpayService(Vertx vertx) {
		this.vertx = vertx;
	}
	
	public XpayService unifierOrder(String storeNo, String appKey, Handler<AsyncResult<JsonObject>> resultHandler) {
		WebClient client = WebClient.create(vertx);
		String tokenUrl = TOKEN_PATH.replace("{appKey}", appKey);
		client.getAbs(tokenUrl).send(ar -> {
			if (ar.succeeded()) {
				HttpResponse<Buffer> response = ar.result();
				JsonObject body = response.bodyAsJsonObject();
				String token = body.getString("token");
				if(token == null || token.length() == 0) {
					resultHandler.handle(Future.failedFuture("Could not get access token"));
				}
				
				String orderUrl = UNIFIER_ORDER_PATH.replace("{storeId}", storeNo);
				client.postAbs(orderUrl).putHeader("Access_token", token).send(dr -> {
					if(dr.succeeded()) {
						HttpResponse<Buffer> orderRes = dr.result();
						JsonObject orderJson = orderRes.bodyAsJsonObject();
						int status = orderJson.getInteger("status");
						if(200 == status) {
							resultHandler.handle(Future.succeededFuture(orderJson.getJsonObject("data")));
						} else {
							resultHandler.handle(Future.failedFuture(orderJson.getString("message")));
						}
					} else {
						LOGGER.error("unified order failed", dr.cause());
						resultHandler.handle(Future.failedFuture(dr.cause()));
					}
				});
			} else {
					LOGGER.error("getToken failed", ar.cause());
					resultHandler.handle(Future.failedFuture(ar.cause()));
				}
		});
		return this;
	}
	
	public XpayService refund(Order order, Handler<AsyncResult<JsonObject>> resultHandler) {
		WebClient client = WebClient.create(vertx);
		String tokenUrl = TOKEN_PATH.replace("{appKey}", order.getAppKey());
		client.getAbs(tokenUrl).send(ar -> {
			if (ar.succeeded()) {
				HttpResponse<Buffer> response = ar.result();
				JsonObject body = response.bodyAsJsonObject();
				String token = body.getString("token");
				if(token == null || token.length() == 0) {
					resultHandler.handle(Future.failedFuture("Could not get access token"));
				}
				
				String refundUrl = REFUND_PATH.replace("{orderNo}", order.getOrderNo()).replace("{storeId}", order.getStoreCode());
				client.deleteAbs(refundUrl).putHeader("Access_token", token).send(dr -> {
					if(dr.succeeded()) {
						HttpResponse<Buffer> refundRes = dr.result();
						JsonObject refundJson = refundRes.bodyAsJsonObject();
						int status = refundJson.getInteger("status");
						if(200 == status) {
							resultHandler.handle(Future.succeededFuture(refundJson.getJsonObject("data")));
						} else {
							resultHandler.handle(Future.failedFuture(refundJson.getString("message")));
						}
					} else {
						LOGGER.error("refund failed", dr.cause());
						resultHandler.handle(Future.failedFuture(dr.cause()));
					}
				});
			} else {
					LOGGER.error("getToken failed", ar.cause());
					resultHandler.handle(Future.failedFuture(ar.cause()));
				}
		});
		return this;
	}

}
