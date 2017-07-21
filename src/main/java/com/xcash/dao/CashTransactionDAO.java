package com.xcash.dao;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xcash.entity.CashTransaction;
import com.xcash.verticles.CashVerticle;

public class CashTransactionDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);

	private final SQLClient client;

	private static final String SQL_INSERT = "INSERT INTO cash_transaction (order_no, channel, card_num, account_name, bank_name, total_fee) VALUES (?, ?, ?, ?, ?, ?)";
	private static final String SQL_UPDATE_STATUS = "UPDATE cash_transaction SET status = ?, update_date = now() WHERE order_no = ? AND deleted = false";
	private static final String SQL_SELECT_ONE = "SELECT order_no, channel, card_num, account_name, bank_name, total_fee, status, update_date FROM cash_transaction WHERE order_no = ? AND deleted = false";
	
	public CashTransactionDAO(JsonObject config) {
		this(Vertx.vertx(), config);
	}

	public CashTransactionDAO(Vertx vertx, JsonObject config) {
		this.client = PostgreSQLClient.createShared(vertx, config, "xcash");
	}

	public CashTransactionDAO insert(CashTransaction transaction, Handler<AsyncResult<Void>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				JsonArray data = new JsonArray().add(transaction.getOrderId())
						.add(transaction.getChannel().getId())
						.add(transaction.getCardNum())
						.add(transaction.getAccountName())
						.add(transaction.getBankName())
						.add(transaction.getTotalFee());
				connection.updateWithParams(SQL_INSERT, data, res -> {
					connection.close();
					if (res.succeeded()) {
						resultHandler.handle(Future.succeededFuture());
					} else {
						LOGGER.error("Insert error", res.cause());
						resultHandler.handle(Future.failedFuture(res.cause()));
					}
				});
			} else {
				LOGGER.error("Insert error", car.cause());
				resultHandler.handle(Future.failedFuture(car.cause()));
			}
		});
		return this;
	}
	
	public CashTransactionDAO updateStatus(String orderId, String status, Handler<AsyncResult<Void>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				JsonArray data = new JsonArray().add(status).add(orderId);
				connection.updateWithParams(SQL_UPDATE_STATUS, data, res -> {
					connection.close();
					if (res.succeeded()) {
						resultHandler.handle(Future.succeededFuture());
					} else {
						LOGGER.error("UpdateStatus error", res.cause());
						resultHandler.handle(Future.failedFuture(res.cause()));
					}
				});
			} else {
				LOGGER.error("UpdateStatus error", car.cause());
				resultHandler.handle(Future.failedFuture(car.cause()));
			}
		});
		return this;
	}
	
	public CashTransactionDAO findByOrderNo(String orderId, Handler<AsyncResult<JsonObject>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				JsonArray data = new JsonArray().add(orderId);
				connection.queryWithParams(SQL_SELECT_ONE, data, res -> {
					connection.close();
					if (res.succeeded()) {
						JsonObject response = new JsonObject();
			            ResultSet resultSet = res.result();
			            if (resultSet.getNumRows() == 0) {
			              response.put("found", false);
			            } else {
			              response.put("found", true);
			              JsonArray row = resultSet.getResults().get(0);
			              response.put("order_no", row.getString(0));
			              response.put("channel", row.getString(1));
			              response.put("card_num", row.getString(2));
			              response.put("account_name", row.getString(3));
			              response.put("bank_name", row.getString(4));
			              response.put("total_fee", row.getString(5));
			              response.put("status", row.getString(6));
			              response.put("update_date", row.getString(7));
			            }
			            resultHandler.handle(Future.succeededFuture(response));
					} else {
						LOGGER.error("UpdateStatus error", res.cause());
						resultHandler.handle(Future.failedFuture(res.cause()));
					}
				});
			} else {
				LOGGER.error("UpdateStatus error", car.cause());
				resultHandler.handle(Future.failedFuture(car.cause()));
			}
		});
		return this;
	}

}
