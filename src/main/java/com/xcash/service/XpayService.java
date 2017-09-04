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
	
	private final Vertx vertx;

	public XpayService(Vertx vertx) {
		this.vertx = vertx;
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
						resultHandler.handle(Future.succeededFuture(dr.result().bodyAsJsonObject()));
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
