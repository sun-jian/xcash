package com.xcash.entity;

public class StoreChannel {
	private long id;
	private String extStoreId;
	private String paymentGateway;
	private String paymentGatewayName;
	private String billType;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
		} else if("CHINAUMSH5".equals(paymentGateway)) {
			this.paymentGatewayName = "银商H5";
		} else if("CHINAUMSWAP".equals(paymentGateway)) {
			this.paymentGatewayName = "银商H5";
		} else if("UPAY".equals(paymentGateway)) {
			this.paymentGatewayName = "收钱吧H5";
		}
	}public String getPaymentGatewayName() {
		return paymentGatewayName;
	}
	public void setPaymentGatewayName(String paymentGatewayName) {
		this.paymentGatewayName = paymentGatewayName;
	}
	public String getBillType() {
		return billType;
	}
	public void setBillType(String billType) {
		this.billType = billType;
	}
}
