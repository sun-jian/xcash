package com.xcash.service;

import io.vertx.core.Future;

import com.xcash.entity.CashTransaction;

public class CashServiceImpl implements CashService {

	public Future<String> postCash(CashTransaction transaction) {
		Future<String> future = Future.future();
		
		future.complete("<ok>200</ok>");
		
		return future;
	}

}
