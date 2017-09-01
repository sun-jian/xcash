package com.xcash.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import com.xcash.entity.Order;
import com.xcash.verticles.CashVerticle;

public class XpayService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);
	private static final String TOKEN_PATH="http://106.14.47.193/xpay/tokens/{appKey}";
	private static final String REFUND_PATH="http://106.14.47.193/xpay/rest/v1/pay/refund/{orderNo}?storeId={storeId}";
	
	private final Vertx vertx;

	public XpayService(Vertx vertx) {
		this.vertx = vertx;
	}
	public XpayService refund(Order order, Handler<AsyncResult<JsonObject>> resultHandler) {
		WebClient client = WebClient.create(vertx);
		HttpRequest<Buffer> get = client.getAbs(TOKEN_PATH);
		JsonObject body = new JsonObject();
		body.put("orderStatus", 1);
		resultHandler.handle(Future.succeededFuture(body));
		return this;
	}

}
