package com.xcash.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import com.xcash.entity.CashTransaction;

public interface CashService {
	CashService postCash(CashTransaction transaction, Handler<AsyncResult<JsonObject>> resultHandler);
	
	CashService query(CashTransaction transaction, Handler<AsyncResult<JsonObject>> resultHandler);
	
	CashService balance(CashTransaction transaction, Handler<AsyncResult<JsonObject>> resultHandler);
}
