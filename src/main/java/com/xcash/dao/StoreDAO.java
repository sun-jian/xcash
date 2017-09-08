package com.xcash.dao;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xcash.entity.Store;
import com.xcash.entity.StoreChannel;
import com.xcash.verticles.CashVerticle;

public class StoreDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);
	
	private final SQLClient client;

	private static final String SQL_SELECT_ALL_STORE = "SELECT a.id, a.code, a.name, a.bail_percentage, a.bail_store_id, a.app_id, a.daily_limit, a.update_date, b.app_key from bill_store a, bill_app b where a.app_id=b.id and a.deleted=false and a.bail_store_id is not null";
	private static final String SQL_SELECT_ALL_STORE_CHANNEL = "SELECT a.store_id, a.ext_store_id, a.payment_gateway, a.bill_type from bill_store_channel a, bill_store b where a.store_id=b.id and a.deleted=false and b.deleted=false";
	
	public StoreDAO(SQLClient client) {
		this(Vertx.vertx(), client);
	}

	public StoreDAO(Vertx vertx, SQLClient client) {
		this.client = client;
	}
	
	public StoreDAO findAllStores(Handler<AsyncResult<Optional<List<Store>>>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				connection.query(SQL_SELECT_ALL_STORE, res -> {
					connection.close();
					if (res.succeeded()) {
						ResultSet resultSet = res.result();
						List<Store> list = new ArrayList<Store>();
			            if (resultSet.getNumRows() > 0) {
			            	for(JsonArray row : resultSet.getResults()) {
			            		Store store = new Store();
			            		store.setId(row.getLong(0));
			            		store.setCode(row.getString(1));
			            		store.setName(row.getString(2));
			            		store.setBailPercentage(row.getInteger(3));
			            		store.setBailStoreId(row.getLong(4));
			            		store.setAppId(row.getLong(5));
			            		long limit = row.getLong(6);
			            		limit = limit == -1L?limit:limit/10000L;
			            		store.setDailyLimit(limit);
			            		LocalDateTime ldt = LocalDateTime.ofInstant(row.getInstant(7), ZoneId.systemDefault());
			            		store.setUpdateDate(ldt.toString());
			            		store.setAppKey(row.getString(8));
			            		list.add(store);
			            	}
			            }
			            resultHandler.handle(Future.succeededFuture(Optional.ofNullable(list)));
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
	
	public StoreDAO findAllStoreChannels(Handler<AsyncResult<Optional<Map<Long, List<StoreChannel>>>>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				connection.query(SQL_SELECT_ALL_STORE_CHANNEL, res -> {
					connection.close();
					if (res.succeeded()) {
						ResultSet resultSet = res.result();
						Map<Long, List<StoreChannel>> map = new HashMap<Long, List<StoreChannel>>();
			            if (resultSet.getNumRows() > 0) {
			            	for(JsonArray row : resultSet.getResults()) {
			            		StoreChannel channel = new StoreChannel();
			            		long storeId = row.getLong(0);
			            		List<StoreChannel> channels = map.get(storeId);
			            		if(channels == null) {
			            			channels = new ArrayList<StoreChannel>();
			            			map.put(storeId,channels);
			            		}
			            		channel.setExtStoreId(row.getString(1));
			            		channel.setPaymentGateway(row.getString(2));
			            		channel.setBillType(row.getString(3));
			            		channels.add(channel);			            	}
			            }
			            resultHandler.handle(Future.succeededFuture(Optional.ofNullable(map)));
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
