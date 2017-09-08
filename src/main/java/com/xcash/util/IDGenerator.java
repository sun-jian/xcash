package com.xcash.util;

import java.security.SecureRandom;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class IDGenerator {
	private static final char X = 'X';
	public static String buildShortOrderNo() {
		StringBuffer sb = new StringBuffer();
		sb.append(X);
		sb.append(TimeUtils.formatTime(new Date(), TimeUtils.TimePattern14));
		sb.append(randomNum(5));
		return sb.toString();
	}

	private static String randomNum(int len) {
		StringBuffer sb = new StringBuffer();
		Random random = new Random();
		for (int i = 0; i < len; i++) {
			sb.append(random.nextInt(10));
		}
		return sb.toString();
	}
	
	private static final String symbols = "abcdefghijklmnopqrstuvwxyzABCDEFGJKLMNPRSTUVWXYZ0123456789";

	public static final String buildKey(int length) {
		Random secureRandomProvider = new SecureRandom();
		char[] buffer = new char[length];
		for (int idx = 0; idx < buffer.length; ++idx)
			buffer[idx] = symbols.charAt(secureRandomProvider.nextInt(symbols
					.length()));
		return new String(buffer);
	}
	
	public static final String buildAuthKey() {
		return UUID.randomUUID().toString();
	}

	public static final String buildAuthSecret() {
		return  buildKey(88);
	}

	
	private static final char T = 'T';
	public static String buildStoreCode() {
		StringBuffer sb = new StringBuffer();
		sb.append(T);
		sb.append(TimeUtils.formatTime(new Date(), TimeUtils.TimePattern14));
		sb.append(randomNum(3));
		return sb.toString();
	}
}
