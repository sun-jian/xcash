package com.xcash.util;

import java.util.Date;
import java.util.Random;

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
}
