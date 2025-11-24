package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.fcm.service.HqAlertStubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * HQ 테스트용 스텁 FCM API 컨트롤러.
 *
 * <p>개발/테스트 환경에서 본사 알림 전송 로직(재고부족, 유통기한 임박)을 간단히 호출해
 * 전송 시뮬레이션 및 로그/ID 반환을 확인하는 용도의 엔드포인트를 제공한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@RestController
@RequestMapping("/fcm/hq-alert/test")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
@Tag(name = "HQ FCM 테스트", description = "개발/QA용 본사 알림 전송 스텁 API")
public class HqAlertStubController {

	private final HqAlertStubService stub;

	/**
	 * 본사 재고 부족 테스트 알림 전송(스텁).
	 *
	 * @param materialName 재료명
	 * @param qty          현재 수량
	 * @param threshold    임계값
	 * @return 전송된 메시지 ID를 담은 맵
	 */
	@Operation(summary = "테스트: 본사 재고 부족 알림 전송",
			description = "테스트용으로 재고 부족 알림을 전송하고 생성된 메시지 ID를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "전송 성공"),
			@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@PostMapping("/stock-low")
	public ResponseEntity<Map<String, String>> stockLow(@RequestParam String materialName,
														@RequestParam long qty,
														@RequestParam long threshold) {
		String id = stub.sendHqStockLow(materialName, qty, threshold);
		return ResponseEntity.ok(Map.of("messageId", id));
	}

	/**
	 * 본사 유통기한 임박 테스트 알림 전송(스텁).
	 *
	 * @param materialName 재료명
	 * @param days         남은 일수
	 * @param lot          로트(배치) 정보
	 * @return 전송된 메시지 ID를 담은 맵
	 */
	@Operation(summary = "테스트: 본사 유통기한 임박 알림 전송",
			description = "테스트용으로 유통기한 임박 알림을 전송하고 생성된 메시지 ID를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "전송 성공"),
			@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@PostMapping("/expire-soon")
	public ResponseEntity<Map<String, String>> expireSoon(@RequestParam String materialName,
														  @RequestParam int days,
														  @RequestParam String lot) {
		String id = stub.sendHqExpireSoon(materialName, days, lot);
		return ResponseEntity.ok(Map.of("messageId", id));
	}
}
