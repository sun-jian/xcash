package com.xcash.service;

import io.vertx.core.Future;

import com.xcash.entity.CashTransaction;

public interface CashService {
	Future<String> postCash(CashTransaction transaction);
}
