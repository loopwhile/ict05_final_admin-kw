package com.boot.ict05_final_admin.domain.fcm.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Web Push 관련 기본값을 외부 설정으로 바인딩하는 클래스.
 *
 * <p>아이콘/배지 경로, TTL, 긴급도, 기본 딥링크 등의 값을 프로퍼티로 관리한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Component
@ConfigurationProperties(prefix = "fcm.webpush")
@Getter
@Setter
public class FcmWebpushProperties {
	/**
	 * 브라우저 알림 아이콘 경로
	 */
	private String icon = "/admin/images/fcm/toastlab.png";

	/**
	 * 배지/모노 아이콘 경로(선택)
	 */
	private String badge = "/admin/images/fcm/badge-72.png";

	/**
	 * Push Service 보관 TTL(초)
	 */
	private int ttlSeconds = 3600;

	/**
	 * 긴급도 (high | normal | low | very-low)
	 */
	private String urgency = "high";

	/**
	 * data.link이 없을 때 사용할 기본 링크
	 */
	private String defaultLink = "/admin";
}
