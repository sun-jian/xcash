package com.xcash.entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.xcash.util.IDGenerator;

import io.vertx.core.json.JsonObject;

public class CashTransaction {
	private Channel channel;
	private TransactionCode tc;
	private String cardNum;
	private String accountName;
	private String idType;
	private String idNumber;
	private String serialNumber;
	private String bankName;
	private String totalFee;
	private String purpose;
	private String orderId;
	
	public CashTransaction() {
		
	}
	
	public CashTransaction(String jsonStr) {
		JsonObject jsonObj = new JsonObject(jsonStr);
		String channelName = jsonObj.getString("channel", "juzhen");
		this.channel = Channel.fromValue(channelName).get();
		this.cardNum = jsonObj.getString("cardNum", "");
		this.accountName = jsonObj.getString("accountName", "");
		this.idType = jsonObj.getString("idType", "");
		this.idNumber = jsonObj.getString("idNumber", "");
		this.serialNumber = jsonObj.getString("serialNumber", "");
		this.bankName = jsonObj.getString("bankName", "");
		this.totalFee = jsonObj.getString("totalFee", "");
		this.purpose = jsonObj.getString("purpose", "");
		this.orderId = IDGenerator.buildShortOrderNo();
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public TransactionCode getTc() {
		return tc;
	}

	public void setTc(TransactionCode tc) {
		this.tc = tc;
	}


	public String getCardNum() {
		return cardNum;
	}

	public void setCardNum(String cardNum) {
		this.cardNum = cardNum;
	}

	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getIdType() {
		return idType;
	}

	public void setIdType(String idType) {
		this.idType = idType;
	}

	public String getIdNumber() {
		return idNumber;
	}

	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getTotalFeeInCent() {
		float fee = NumberUtils.toFloat(totalFee, 0);
		int feeInCent = (int)(fee * 100);
		return String.valueOf(feeInCent);
	}

	public boolean isValid() {
		if(tc == null) {
			return false;
		}
		switch(tc) {
		case CASHING: 
			return StringUtils.isNoneBlank(cardNum, accountName, bankName, totalFee, purpose);
		}
		
		return true;
	}
}
