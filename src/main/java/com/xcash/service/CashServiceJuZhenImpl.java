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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ju.utils.EncryptUtils;
import com.xcash.entity.CashTransaction;
import com.xcash.util.IDGenerator;
import com.xcash.util.TimeUtils;
import com.xcash.verticles.CashVerticle;

public class CashServiceJuZhenImpl implements CashService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);
	
	private final Vertx vertx;

	public CashServiceJuZhenImpl(Vertx vertx) {
		this.vertx = vertx;
	}

	public CashService postCash(CashTransaction transaction, Handler<AsyncResult<JsonObject>> resultHandler) {
		String storeId = transaction.getChannel().getStoreId();
		String tradeCode = transaction.getChannel().getTransactionCode(transaction.getTc());
		String orderId = transaction.getOrderId();
		String backUrl = transaction.getChannel().getNotifyUrl();
		String endpoint = transaction.getChannel().getEndpoint();
		String pubKeyUrl = transaction.getChannel().getPublicKeyPath();
		
		final String msgInfo = this.buildCashingMsg(transaction);
		this.encrypt(msgInfo, pubKeyUrl, res -> {
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
				LOGGER.info("PostCash request: {}, {}", endpoint , msg);
				WebClient client = WebClient.create(vertx);
				client.postAbs(endpoint).sendBuffer(buffer, ar -> {
					if (ar.succeeded()) {
						HttpResponse<Buffer> response = ar.result();
						JsonObject body = response.bodyAsJsonObject();
						LOGGER.info("PostCash result: {}", body.encode());
						JsonObject result = new JsonObject();
						result.put("orderId", orderId);
						String code = body.getString("respCode");
						String status = this.handleStatus(code);
						result.put("status", status);
						if(!CashTransaction.SUCCESS.equals(status)) {
							result.put("error", body.getString("respInfo"));
						}
						resultHandler.handle(Future.succeededFuture(result));
					} else {
						LOGGER.error("postCash failed", ar.cause());
						resultHandler.handle(Future.failedFuture(ar.cause()));
					}
			    });
			});
		});
		return this;
	}
	
	public CashService query(CashTransaction transaction, Handler<AsyncResult<JsonObject>> resultHandler) {
		String storeId = transaction.getChannel().getStoreId();
		String tradeCode = transaction.getChannel().getTransactionCode(transaction.getTc());
		String orderId = IDGenerator.buildShortOrderNo();
		String endpoint = transaction.getChannel().getEndpoint();
		String pubKeyUrl = transaction.getChannel().getPublicKeyPath();
		
		final String msgInfo = this.buildQueryMsg(transaction);
		this.encrypt(msgInfo, pubKeyUrl, res -> {
			this.sign(storeId, orderId, tradeCode, res.result(), pubKeyUrl, sign -> {
				Map<String, String> map = new HashMap<String, String>();
				map.put("merId", storeId);
				map.put("tradeCode", tradeCode);
				map.put("orderId", orderId);
				map.put("msg", res.result());
				map.put("signature", sign.result());
				String msg = this.toMsg(map);
				Buffer buffer = Buffer.buffer(msg);
				LOGGER.info("Query request: {}, {}", endpoint , msg);
				WebClient client = WebClient.create(vertx);
				client.postAbs(endpoint).sendBuffer(buffer, ar -> {
					if (ar.succeeded()) {
						HttpResponse<Buffer> response = ar.result();
						JsonObject body = response.bodyAsJsonObject();
						LOGGER.info("Query result: {}", body.encode());
						JsonObject result = new JsonObject();
						result.put("orderId", orderId);
						String code = body.getString("respCode");
						String ordStatus = body.getString("ordStatus");
						String status = this.handleStatus(code, ordStatus);
						result.put("status", status);
						if(!CashTransaction.SUCCESS.equals(status)) {
							result.put("error", body.getString("ordInfo"));
						}
						resultHandler.handle(Future.succeededFuture(result));
					} else {
						LOGGER.error("postCash failed", ar.cause());
						resultHandler.handle(Future.failedFuture(ar.cause()));
					}
			    });
			});
		});
		return this;
	}
	
	public CashService balance(CashTransaction transaction, Handler<AsyncResult<JsonObject>> resultHandler) {
		String storeId = transaction.getChannel().getStoreId();
		String tradeCode = transaction.getChannel().getTransactionCode(transaction.getTc());
		String orderId = transaction.getOrderId();
		String endpoint = transaction.getChannel().getEndpoint();
		String pubKeyUrl = transaction.getChannel().getPublicKeyPath();
		
		final String msgInfo = this.buildBalanceMsg(transaction);
		this.encrypt(msgInfo, pubKeyUrl, res -> {
			this.sign(storeId, orderId, tradeCode, res.result(), pubKeyUrl, sign -> {
				Map<String, String> map = new HashMap<String, String>();
				map.put("merId", storeId);
				map.put("tradeCode", tradeCode);
				map.put("orderId", orderId);
				map.put("msg", res.result());
				map.put("signature", sign.result());
				String msg = this.toMsg(map);
				Buffer buffer = Buffer.buffer(msg);
				LOGGER.info("Balance request: {}, {}", endpoint , msg);
				WebClient client = WebClient.create(vertx);
				client.postAbs(endpoint).sendBuffer(buffer, ar -> {
					if (ar.succeeded()) {
						HttpResponse<Buffer> response = ar.result();
						JsonObject body = response.bodyAsJsonObject();
						body.put("orderId", orderId);
						LOGGER.info("Balance result: {}", body.encode());
						resultHandler.handle(Future.succeededFuture(body));
					} else {
						LOGGER.error("postCash failed", ar.cause());
						resultHandler.handle(Future.failedFuture(ar.cause()));
					}
			    });
			});
		});
		return this;
	}

	private void encrypt(final String msgInfo,final String pubKeyUrl,
			Handler<AsyncResult<String>> handler) {
		vertx.executeBlocking(future -> {
			try {
				LOGGER.info("before encrypt msg = {}", msgInfo);
				String msg = EncryptUtils.encrypt(msgInfo, pubKeyUrl);
				LOGGER.info("after encrypt msg = {}", msg);
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
						LOGGER.info("sign = {}", signature);
						future.complete(signature);
					} catch (Exception e) {
						future.fail(e);
					}
				}, handler);
	}
	
	private static final char SEP = '|';
	private static final String ACCOUNT_PRIVATE = "02";
	private String buildCashingMsg(CashTransaction transaction) {
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
	
	private String buildQueryMsg(CashTransaction transaction) {
		StringBuilder sb = new StringBuilder();
		sb.append(TimeUtils.formatTime(new Date(),TimeUtils.TimePattern14));
		sb.append(SEP).append(transaction.getOrderId());
		sb.append(SEP).append(transaction.getChannel().getCashTransCode());
		return sb.toString();
	}
	
	private String buildBalanceMsg(CashTransaction transaction) {
		StringBuilder sb = new StringBuilder();
		sb.append(TimeUtils.formatTime(new Date(),TimeUtils.TimePattern14));
		sb.append(SEP).append("01");
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
	
	private String handleStatus(String code) {
		String status = CashTransaction.SUCCESS;
		if(code.startsWith("0")) {
			status = CashTransaction.SUCCESS;
		} else if(code.startsWith("1")) {
			status = CashTransaction.ERROR;
		} else {
			status = CashTransaction.PENDING;
		}
		return status;
	}
	
	private String handleStatus(String code, String ordStatus) {
		String status = CashTransaction.SUCCESS;
		if(code.startsWith("0") && ordStatus.startsWith("0")) {
			status = CashTransaction.SUCCESS;
		} else if(code.startsWith("1") || ordStatus.startsWith("1")) {
			status = CashTransaction.ERROR;
		} else {
			status = CashTransaction.PENDING;
		}
		return status;
	}

}
