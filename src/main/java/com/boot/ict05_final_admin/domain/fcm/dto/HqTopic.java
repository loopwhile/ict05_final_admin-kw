package com.boot.ict05_final_admin.domain.fcm.dto;

import java.util.Arrays;

/**
 * 본사에서 사용되는 FCM 토픽의 열거형.
 *
 * <p>열거값은 실제 토픽 문자열을 보유하며, 허용 여부 확인 유틸리티를 제공한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
public enum HqTopic {
	HQ_ALL("hq-all"),
	STOCK_LOW("hq-stock-low"),
	EXPIRE_SOON("hq-expire-soon");

	private final String value;

	HqTopic(String value) { this.value = value; }

	public String value() { return value; }

	/**
	 * 주어진 토픽 문자열이 허용된 토픽 목록에 포함되는지 여부를 반환한다.
	 *
	 * @param topic 검사할 토픽 문자열
	 * @return 허용되면 true
	 */
	public static boolean isAllowed(String topic) {
		return Arrays.stream(values()).anyMatch(t -> t.value.equals(topic));
	}
}
