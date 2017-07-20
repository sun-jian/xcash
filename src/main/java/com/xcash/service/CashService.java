package com.xcash.service;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import com.xcash.entity.CashTransaction;

public interface CashService {
	Future<JsonObject> postCash(CashTransaction transaction);
}
