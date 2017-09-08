package com.xcash.entity;

import java.util.List;


public class Store {
	private long id;
	private String code;
	private String name;
	private int bailPercentage;
	private long bailStoreId;
	private List<StoreChannel> bailStoreChannels;
	private long appId;
	private String appKey;
	private String appSecret;
	private long dailyLimit;
	private String updateDate;
	private List<StoreChannel> channels;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getBailPercentage() {
		return bailPercentage;
	}
	public void setBailPercentage(int bailPercentage) {
		this.bailPercentage = bailPercentage;
	}
	public long getBailStoreId() {
		return bailStoreId;
	}
	public void setBailStoreId(long bailStoreId) {
		this.bailStoreId = bailStoreId;
	}
	public List<StoreChannel> getBailStoreChannels() {
		return bailStoreChannels;
	}
	public void setBailStoreChannels(List<StoreChannel> bailStoreChannels) {
		this.bailStoreChannels = bailStoreChannels;
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
	public String getAppSecret() {
		return appSecret;
	}
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
	public long getDailyLimit() {
		return dailyLimit;
	}
	public void setDailyLimit(long dailyLimit) {
		this.dailyLimit = dailyLimit;
	}
	public String getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}
	public List<StoreChannel> getChannels() {
		return channels;
	}
	public void setChannels(List<StoreChannel> channels) {
		this.channels = channels;
	}
}
