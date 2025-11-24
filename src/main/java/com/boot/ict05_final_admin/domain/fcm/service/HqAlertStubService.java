package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.dto.HqTopic;
import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * HQ 알림 템플릿 발송을 검증하는 스텁 서비스.
 *
 * <p>템플릿 렌더링과 토픽 발송이 정상 동작하는지 간단히 점검할 수 있는 유틸리티를 제공한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Service
@RequiredArgsConstructor
public class HqAlertStubService {

	private final FcmService fcmService;

	/**
	 * HQ 재고 부족 알림을 스텁으로 전송한다.
	 *
	 * @param materialName 재료명
	 * @param qty          현재 수량
	 * @param threshold    임계 수량
	 * @return 메시지 ID
	 */
	public String sendHqStockLow(String materialName, long qty, long threshold) {
		var vars = Map.<String, Object>of(
				"materialName", materialName,
				"qty",          qty,
				"threshold",    threshold
		);

		var title = fcmService.renderTitle("HQ_STOCK_LOW", vars);
		var body  = fcmService.renderBody ("HQ_STOCK_LOW", vars);

		Map<String, String> data = new HashMap<>();
		data.put("type", "HQ_STOCK_LOW");
		data.put("materialName", materialName);
		data.put("link", "/admin/inventory/list");

		return fcmService.sendToTopic(AppType.HQ, HqTopic.STOCK_LOW, title, body, data);
	}

	/**
	 * HQ 유통기한 임박 알림을 스텁으로 전송한다.
	 *
	 * @param materialName 재료명
	 * @param days         남은 일수
	 * @param lot          로트 코드
	 * @return 메시지 ID
	 */
	public String sendHqExpireSoon(String materialName, int days, String lot) {
		var vars = Map.<String, Object>of(
				"materialName", materialName,
				"days",         days,
				"lot",          lot
		);

		var title = fcmService.renderTitle("HQ_EXPIRE_SOON", vars);
		var body  = fcmService.renderBody ("HQ_EXPIRE_SOON", vars);

		Map<String, String> data = new HashMap<>();
		data.put("type", "HQ_EXPIRE_SOON");
		data.put("materialName", materialName);
		data.put("days", String.valueOf(days));
		data.put("lot", lot);
		data.put("link", "/admin/inventory/list");

		return fcmService.sendToTopic(AppType.HQ, HqTopic.EXPIRE_SOON, title, body, data);
	}
}
