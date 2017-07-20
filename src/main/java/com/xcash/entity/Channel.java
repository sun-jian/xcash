package com.xcash.entity;

import java.util.Arrays;
import java.util.Optional;

public enum Channel {
	JUZHEN_TEST("juzhen_test", "898000000020195", "http://tech.eidpay.com:8080/juPay", "/var/certs/juzhen/898000000020195_public.key", "500302", "500401", "500501", "http://106.14.47.193/xcash/notify/juzhen_test"),
	JUZHEN("juzhen", "898000000020068", "https://app.eidpay.com/juPay", "/var/certs/juzhen/898000000020068_public.key", "500302", "500401", "500501", "http://106.14.47.193/xcash/notify/juzhen");
	
	String id;
	String storeId;
	String endpoint;
	String publicKeyPath;
	String cashTransCode;
	String balanceQueryCode;
	String orderQueryCode;
	String notifyUrl;
	
	Channel(String id, String storeId, String endpoint, String publicKeyPath, String cashTransCode, String balanceQueryCode, String orderQueryCode, String notifyUrl) {
		this.id = id;
		this.storeId = storeId;
		this.endpoint = endpoint;
		this.publicKeyPath = publicKeyPath;
		this.cashTransCode = cashTransCode;
		this.balanceQueryCode = balanceQueryCode;
		this.orderQueryCode = orderQueryCode;
		this.notifyUrl = notifyUrl;
	}

	public static Optional<Channel> fromValue(String val) {
		Channel[] channels = Channel.values();
		return Arrays.stream(channels).filter(x -> x.getId().equals(val)).findFirst();
	}
	
	public String getId() {
		return id;
	}

	public String getStoreId() {
		return storeId;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public String getPublicKeyPath() {
		return publicKeyPath;
	}

	public String getCashTransCode() {
		return cashTransCode;
	}

	public String getBalanceQueryCode() {
		return balanceQueryCode;
	}

	public String getOrderQueryCode() {
		return orderQueryCode;
	}	
	
	public String getNotifyUrl() {
		return notifyUrl;
	}
	
	public String getTransactionCode(TransactionCode code) {
		switch(code) {
			case CASHING: return this.getCashTransCode();
			case BALANCE: return this.getBalanceQueryCode();
			case QUERY: return this.getOrderQueryCode();
			default: throw new java.lang.IllegalArgumentException();
		}
	}
}
