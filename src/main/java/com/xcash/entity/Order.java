package com.xcash.entity;

public class Order {
	private String orderNo;
	private String extOrderNo;
	private String returnUrl;
	private String targetOrderNo;
	private String totalFee;
	private String status;
	private long appId;
	private String appKey;
	private long storeId;
	private long channelId;
	private String extStoreId;
	private String paymentGateway;
	private String paymentGatewayName;
	private String storeCode;
	private String storeName;
	private String updateDate;
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getExtOrderNo() {
		return extOrderNo;
	}
	public void setExtOrderNo(String extOrderNo) {
		this.extOrderNo = extOrderNo;
	}
	public String getReturnUrl() {
		return returnUrl;
	}
	public void setReturnUrl(String returnUrl) {
		this.returnUrl = returnUrl;
	}
	public String getTargetOrderNo() {
		return targetOrderNo;
	}
	public void setTargetOrderNo(String targetOrderNo) {
		this.targetOrderNo = targetOrderNo;
	}
	public String getTotalFee() {
		return totalFee;
	}
	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public long getAppId() {
		return appId;
	}
	public void setAppId(long appId) {
		this.appId = appId;
	}
	public String getAppKey() {
		return appKey;
	}
	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}
	public String getStoreCode() {
		return storeCode;
	}
	public void setStoreCode(String storeCode) {
		this.storeCode = storeCode;
	}
	public String getStoreName() {
		return storeName;
	}
	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}
	public long getStoreId() {
		return storeId;
	}
	public void setStoreId(long storeId) {
		this.storeId = storeId;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public long getChannelId() {
		return channelId;
	}
	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}
	public String getExtStoreId() {
		return extStoreId;
	}
	public void setExtStoreId(String extStoreId) {
		this.extStoreId = extStoreId;
	}
	public String getPaymentGateway() {
		return paymentGateway;
	}
	public void setPaymentGateway(String paymentGateway) {
		this.paymentGateway = paymentGateway;
		if("CHINAUMS".equals(paymentGateway)) {
			this.paymentGatewayName = "银商悦单";
		} else if("CHINAUMSV2".equals(paymentGateway)) {
			this.paymentGatewayName = "银商C扫B";
		} else if("JUZHEN".equals(paymentGateway)) {
			this.paymentGatewayName = "钜真";
		}
	}
	public String getPaymentGatewayName() {
		return paymentGatewayName;
	}
	public void setPaymentGatewayName(String paymentGatewayName) {
		this.paymentGatewayName = paymentGatewayName;
	}
}
