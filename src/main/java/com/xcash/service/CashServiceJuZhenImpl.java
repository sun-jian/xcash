package com.xcash.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ju.utils.EncryptUtils;
import com.xcash.entity.CashTransaction;
import com.xcash.util.TimeUtils;

public class CashServiceJuZhenImpl implements CashService {
	private final Vertx vertx;

	public CashServiceJuZhenImpl(Vertx vertx) {
		this.vertx = vertx;
	}

	public Future<JsonObject> postCash(CashTransaction transaction) {
		Future<JsonObject> result = Future.future();
		String storeId = transaction.getChannel().getStoreId();
		String tradeCode = transaction.getChannel().getTransactionCode(transaction.getTc());
		String orderId = transaction.getOrderId();
		String backUrl = transaction.getChannel().getNotifyUrl();
		String endpoint = transaction.getChannel().getEndpoint();
		String pubKeyUrl = transaction.getChannel().getPublicKeyPath();
		
		final String msgInfo = this.buildMsg(transaction);
		this.encrity(msgInfo, pubKeyUrl, res -> {
			this.sign(storeId, orderId, tradeCode, res.result(), pubKeyUrl, sign -> {
				Map<String, String> map = new HashMap<String, String>();
				map.put("merId", storeId);
				map.put("tradeCode", tradeCode);
				map.put("orderId", orderId);
				map.put("msg", res.result());
				map.put("signature", sign.result());
				map.put("backUrl", backUrl);
				String msg = this.toMsg(map);
				Buffer buffer = Buffer.buffer(msg);
				System.out.println("post request: " + endpoint + ", " + msg);
				WebClient client = WebClient.create(vertx);
				client.postAbs(endpoint).ssl(endpoint.startsWith("https:"))
					  .sendBuffer(buffer, ar -> {
					if (ar.succeeded()) {
						HttpResponse<Buffer> response = ar.result();
						JsonObject body = response.bodyAsJsonObject();
						System.out.println("PostCash : "+ body.encode());
						result.complete(body);
					} else {
						ar.cause().printStackTrace();
						result.fail(ar.cause());
					}
			    });
			});
		});
		return result;
	}

	private void encrity(final String msgInfo,final String pubKeyUrl,
			Handler<AsyncResult<String>> handler) {
		vertx.executeBlocking(future -> {
			try {
				System.out.println("before msg = " + msgInfo);
				String msg = EncryptUtils.encrypt(msgInfo, pubKeyUrl);
				System.out.println("msg = " + msg);
				future.complete(msg);
			} catch (Exception e) {
				e.printStackTrace();
				future.fail(e);
			}
		}, handler);
	}

	private void sign(final String merId, final String orderId,
			final String tradeCode, final String msgInfo, final String pubKeyUrl,
			Handler<AsyncResult<String>> handler) {
		vertx.executeBlocking(
				future -> {
					try {
						String signature = EncryptUtils.juSignature(merId
								+ orderId + tradeCode + msgInfo, pubKeyUrl);
						System.out.println("sign = " + signature);
						future.complete(signature);
					} catch (Exception e) {
						future.fail(e);
					}
				}, handler);
	}
	
	private static final char SEP = '|';
	private static final String ACCOUNT_PRIVATE = "02";
	private String buildMsg(CashTransaction transaction) {
		StringBuilder sb = new StringBuilder();
		sb.append(TimeUtils.formatTime(new Date(),TimeUtils.TimePattern14));
		sb.append(SEP).append(ACCOUNT_PRIVATE);
		sb.append(SEP).append(transaction.getCardNum());
		sb.append(SEP).append(transaction.getAccountName());
		sb.append(SEP).append(transaction.getIdType());
		sb.append(SEP).append(transaction.getIdNumber());
		sb.append(SEP).append(transaction.getSerialNumber());
		sb.append(SEP).append(transaction.getBankName());
		sb.append(SEP).append(transaction.getTotalFeeInCent());
		sb.append(SEP).append(transaction.getPurpose());
		return sb.toString();
	}

	private String toMsg(Map<String, String> map) {
		StringBuilder sb = new StringBuilder();
		String msg = "";
		if (map != null) {

			for (Entry<String, String> e : map.entrySet()) {

				sb.append(e.getKey());

				sb.append("=");

				sb.append(e.getValue());

				sb.append("&");

			}

			msg = sb.substring(0, sb.length() - 1);

		}
		return msg;
	}

}
