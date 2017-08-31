package com.xcash.dao;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xcash.entity.Order;
import com.xcash.verticles.CashVerticle;

public class OrderDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);
	
	private final SQLClient client;

	private static final String SQL_SELECT_BY_ORDER_NO = "SELECT order_no, ext_order_no, seller_order_no, target_order_no, total_fee, status, app_id, store_id, update_date from bill_order where order_no = ? and deleted=false";
	private static final String SQL_SELECT_BY_EXT_ORDER_NO = "SELECT order_no, ext_order_no, seller_order_no, target_order_no, total_fee, status, app_id, store_id, update_date from bill_order where ext_order_no = ? and deleted=false";
	private static final String SQL_SELECT_BY_SELLER_ORDER_NO = "SELECT order_no, ext_order_no, seller_order_no, target_order_no, total_fee, status, app_id, store_id, update_date from bill_order where seller_order_no = ? and deleted=false";
	private static final String SQL_SELECT_BY_TARGET_ORDER_NO = "SELECT order_no, ext_order_no, seller_order_no, target_order_no, total_fee, status, app_id, store_id, update_date from bill_order where target_order_no = ? and deleted=false";
	private static final String SQL_SELECT_APP_ID = "SELECT app_key from bill_app where id = ?";
	private static final String SQL_SELECT_STORE_BY_ID = "SELECT name from bill_store where id = ?";
	
	
	public OrderDAO(SQLClient client) {
		this(Vertx.vertx(), client);
	}

	public OrderDAO(Vertx vertx, SQLClient client) {
		this.client = client;
	}
	
	public OrderDAO findByOrderNo(String orderNo, Handler<AsyncResult<Optional<Order>>> resultHandler) {
		return findOrder(SQL_SELECT_BY_ORDER_NO, orderNo, resultHandler);
	}
	
	public OrderDAO findByExtOrderNo(String orderNo, Handler<AsyncResult<Optional<Order>>> resultHandler) {
		return findOrder(SQL_SELECT_BY_EXT_ORDER_NO, orderNo, resultHandler);
	}
	
	public OrderDAO findBySellerOrderNo(String orderNo, Handler<AsyncResult<Optional<Order>>> resultHandler) {
		return findOrder(SQL_SELECT_BY_SELLER_ORDER_NO, orderNo, resultHandler);
	}
	
	public OrderDAO findByTargetOrderNo(String orderNo, Handler<AsyncResult<Optional<Order>>> resultHandler) {
		return findOrder(SQL_SELECT_BY_TARGET_ORDER_NO, orderNo, resultHandler);
	}
	
	public OrderDAO findAppId(long appId, Handler<AsyncResult<String>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				JsonArray data = new JsonArray().add(appId);
				connection.queryWithParams(SQL_SELECT_APP_ID, data, res -> {
					connection.close();
					if (res.succeeded()) {
						String appKey = "";
						ResultSet resultSet = res.result();
						
			            if (resultSet.getNumRows() > 0) {
			            	JsonArray row = resultSet.getResults().get(0);
			            	appKey = row.getString(0);
			            }
			            resultHandler.handle(Future.succeededFuture(appKey));
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
	
	public OrderDAO findStoreNameById(long storeId, Handler<AsyncResult<String>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				JsonArray data = new JsonArray().add(storeId);
				connection.queryWithParams(SQL_SELECT_STORE_BY_ID, data, res -> {
					connection.close();
					if (res.succeeded()) {
						String name = "";
						ResultSet resultSet = res.result();
						
			            if (resultSet.getNumRows() > 0) {
			            	JsonArray row = resultSet.getResults().get(0);
			            	name = row.getString(0);
			            }
			            resultHandler.handle(Future.succeededFuture(name));
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
	
	
	private OrderDAO findOrder(String sql, String orderNo, Handler<AsyncResult<Optional<Order>>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				JsonArray data = new JsonArray().add(orderNo);
				connection.queryWithParams(sql, data, res -> {
					connection.close();
					if (res.succeeded()) {
						Order order = null;
						ResultSet resultSet = res.result();
						
			            if (resultSet.getNumRows() > 0) {
			            	JsonArray row = resultSet.getResults().get(0);
			            	order = this.toOrder(row);
			            }
			            resultHandler.handle(Future.succeededFuture(Optional.ofNullable(order)));
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
	
	private Order toOrder(JsonArray row) {
		Order order = new Order();
		order.setOrderNo(row.getString(0));
    	order.setExtOrderNo(row.getString(1));
    	order.setSellerOrderNo(row.getString(2));
    	order.setTargetOrderNo(row.getString(3));
    	order.setTotalFee(row.getString(4));
    	order.setStatus(row.getString(5));
    	order.setAppId(row.getLong(6));
    	order.setStoreId(row.getLong(7));
    	order.setUpdateDate(row.getString(8));
    	return order;
	}

}
