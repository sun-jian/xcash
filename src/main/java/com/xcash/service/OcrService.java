package com.xcash.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xcash.verticles.CashVerticle;
import com.youtu.Youtu;

public class OcrService {
	private static final Logger LOGGER = LoggerFactory.getLogger(CashVerticle.class);
	public static final String APP_ID = "10097831";
	public static final String SECRET_ID = "AKIDqX2OYZe2Uky0ta94va4LUTFVflN73FWb";
	public static final String SECRET_KEY = "1MbtBXlncHk5PaYJsETVLzSnwscAVdYG";
	public static final String USER_ID = "1648734123";
	
	private final Vertx vertx;
	private static final Youtu faceYoutu = new Youtu(APP_ID, SECRET_ID, SECRET_KEY,Youtu.API_YOUTU_END_POINT,USER_ID);
	
	public OcrService(Vertx vertx) {
		this.vertx = vertx;
	}
	
	public OcrService recognize(String imgPath, Handler<AsyncResult<JsonObject>> resultHandler) {
		vertx.executeBlocking(future -> {
			try {
				JSONObject response = faceYoutu.GeneralOcr(imgPath);
				JSONArray items = response.getJSONArray("items");
				String targetOrderNo = "";
				for(int i=0; i< items.length();i++) {
					JSONObject item = (JSONObject)items.get(i);
					String str = item.getString("itemstring");
					if(str!=null && str.length()>=28 && str.startsWith("400")) {
						targetOrderNo = str;
					} 
				}
				JsonObject result = new JsonObject();
				result.put("targetOrderNo", targetOrderNo);
				future.complete(result);
			} catch (Exception e) {
				LOGGER.error("ocr faild", e);
				future.fail(e);
			}
		}, resultHandler);
		return this;
	}
}
