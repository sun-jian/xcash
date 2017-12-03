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
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xcash.entity.Store;
import com.xcash.entity.StoreChannel;
import com.xcash.verticles.CashVerticle;

public class StoreDAO {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);
	
	private final SQLClient client;

	private static final String SQL_SELECT_ALL_STORE = "SELECT a.id, a.code, a.name, a.bail_percentage, a.channels, a.bail_channels,a.app_id, a.daily_limit, a.update_date, b.app_key from bill_store a, bill_app b where a.app_id=b.id and a.deleted=false and a.channels is not null";
	private static final String SQL_SELECT_ONE_STORE = "SELECT id, code, name, bail_percentage, channels,bail_channels, app_id, daily_limit, update_date from bill_store where id=? and deleted=false";
	private static final String SQL_SELECT_ALL_STORE_CHANNEL = "SELECT id, ext_store_id, payment_gateway, bill_type from bill_store_channel where deleted=false";
	private static final String SQL_UPDATE_STORE = "UPDATE bill_store set bail_percentage=?, daily_limit=?, update_date=now() where id=? and bail_percentage=? and daily_limit=?";
	
	
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
			            		store.setChannelList(row.getString(4));
			            		store.setBailChannelList(row.getString(5));
			            		store.setAppId(row.getLong(6));
			            		long limit = row.getLong(7);
			            		limit = limit == -1L?limit:limit/10000L;
			            		store.setDailyLimit(limit);
			            		LocalDateTime ldt = LocalDateTime.ofInstant(row.getInstant(8), ZoneId.systemDefault());
			            		store.setUpdateDate(ldt.toString());
			            		store.setAppKey(row.getString(9));
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
	
	public StoreDAO findAllStoreChannels(Handler<AsyncResult<List<StoreChannel>>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				connection.query(SQL_SELECT_ALL_STORE_CHANNEL, res -> {
					connection.close();
					if (res.succeeded()) {
						ResultSet resultSet = res.result();
						List<StoreChannel> channels = new ArrayList<StoreChannel>();
			            if (resultSet.getNumRows() > 0) {
			            	for(JsonArray row : resultSet.getResults()) {
			            		StoreChannel channel = new StoreChannel();
			            		channel.setId(row.getLong(0));
			            		channel.setExtStoreId(row.getString(1));
			            		channel.setPaymentGateway(row.getString(2));
			            		channel.setBillType(row.getString(3));
			            		channels.add(channel);			            	
			            	}
			            }
			            resultHandler.handle(Future.succeededFuture(channels));
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
	
	public StoreDAO updateStore(Store store, Handler<AsyncResult<Optional<Store>>> resultHandler) {
		client.getConnection(car -> {
			if (car.succeeded()) {
				SQLConnection connection = car.result();
				connection.query(SQL_SELECT_ONE_STORE, res -> {
					ResultSet resultSet = res.result();
					Store dbStore = new Store();
					if (resultSet.getNumRows() > 0) {
						 JsonArray row = resultSet.getResults().get(0);
						 dbStore.setId(row.getLong(0));
						 dbStore.setCode(row.getString(1));
						 dbStore.setName(row.getString(2));
						 dbStore.setBailPercentage(row.getInteger(3));
						 dbStore.setChannelList(row.getString(4));
						 dbStore.setBailChannelList(row.getString(5));
						 dbStore.setAppId(row.getLong(6));
						 dbStore.setDailyLimit(row.getLong(7));
						 LocalDateTime ldt = LocalDateTime.ofInstant(row.getInstant(8), ZoneId.systemDefault());
						 dbStore.setUpdateDate(ldt.toString());
					 }
					 
					 if(dbStore.getBailPercentage()!=store.getBailPercentage() || dbStore.getDailyLimit() != store.getDailyLimit()) {
						 connection.close();
						 JsonArray params = new JsonArray();
						 params.add(store.getBailPercentage()).add(store.getDailyLimit()).add(store.getId()).add(dbStore.getBailPercentage()).add(dbStore.getDailyLimit());
						 connection.updateWithParams(SQL_UPDATE_STORE, params, storeUpdateRes -> {
							 if (res.succeeded()) {
								 
								 
								} else {
									LOGGER.error("UpdateStatus error", res.cause());
									resultHandler.handle(Future.failedFuture(res.cause()));
								}
						 });
					 } else {
						 connection.close();
					 }
				});
			}
		});
		
		return this;
	}
}
