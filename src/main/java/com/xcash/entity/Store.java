package com.xcash.entity;

import io.vertx.core.json.JsonObject;

import java.util.List;


public class Store {
	private long id;
	private String code;
	private String name;
	private int bailPercentage;
	private String bailChannelList;
	private List<StoreChannel> bailStoreChannels;
	private long appId;
	private String appKey;
	private String appSecret;
	private long dailyLimit;
	private String updateDate;
	private List<StoreChannel> channels;
	private String channelList;
	
	public Store() {
		
	}
	
	public Store(JsonObject jsonObj) {
		this.id = jsonObj.getLong("id");
		this.code = jsonObj.getString("code");
		this.name = jsonObj.getString("name");
		this.bailPercentage = jsonObj.getInteger("bailPercentage");
		this.bailChannelList = jsonObj.getString("bailChannelList");
		this.appId = jsonObj.getLong("appId");
		this.appKey = jsonObj.getString("appKey");
		this.appSecret = jsonObj.getString("appSecret");
		this.dailyLimit = jsonObj.getInteger("dailyLimit");
		this.channelList = jsonObj.getString("channelList"); 
	}
	
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
	public String getBailChannelList() {
		return bailChannelList;
	}
	public void setBailChannelList(String bailChannelList) {
		this.bailChannelList = bailChannelList;
	}
	public String getChannelList() {
		return channelList;
	}
	public void setChannelList(String channelList) {
		this.channelList = channelList;
	}
	
	public JsonObject toJson() {
		JsonObject json = JsonObject.mapFrom(this);
		json.remove("channelList");
		json.remove("bailChannelList");
		return json;
	}
}
