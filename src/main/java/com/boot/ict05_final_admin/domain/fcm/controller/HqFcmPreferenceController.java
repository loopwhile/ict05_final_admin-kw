package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.fcm.dto.FcmPreferenceUpdateRequest;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmPreference;
import com.boot.ict05_final_admin.domain.fcm.service.FcmPreferenceService;
import com.boot.ict05_final_admin.domain.fcm.service.FcmService;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * 본사 사용자용 FCM 수신 설정(Preference) 관리 API.
 *
 * <p>HQ 사용자가 자신의 알림 카테고리 수신 설정을 조회/수정할 수 있는 엔드포인트를 제공한다.
 * 설정 변경 시 관련 토픽 구독/해제 동작을 선택적으로 적용할 수 있다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@RestController
@RequestMapping("/fcm/pref")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
@Tag(name = "FCM 수신 설정", description = "본사 사용자의 알림 수신 설정 조회/수정")
public class HqFcmPreferenceController {

	private final FcmPreferenceService prefService;
	private final FcmService fcmService;
	private final MyPageService myPageService;

	/**
	 * 현재 로그인한 HQ 사용자의 수신 설정을 반환한다.
	 *
	 * @param principal 서블릿 프린시펄
	 * @param auth      스프링 시큐리티 인증 객체 (선택)
	 * @return 수신 설정 정보 맵
	 */
	@Operation(summary = "나의 수신 설정 조회", description = "로그인 사용자의 FCM 수신 설정을 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@GetMapping("/me")
	public ResponseEntity<Map<String, Object>> getMine(Principal principal, Authentication auth) {
		Long memberId = extractMemberId(principal, auth);
		FcmPreference p = prefService.getForHqMember(memberId);
		return ResponseEntity.ok(Map.of(
				"memberId", memberId,
				"catNotice",    p != null ? p.getCatNotice()    : true,
				"catStockLow",  p != null ? p.getCatStockLow()  : true,
				"catExpireSoon",p != null ? p.getCatExpireSoon(): true,
				"thresholdDays",p != null ? p.getThresholdDays(): 3
		));
	}

	/**
	 * 현재 로그인한 HQ 사용자의 수신 설정을 저장(업서트)한다.
	 *
	 * <p>요청의 {@code applySubscriptions}가 true(또는 미지정)인 경우에는
	 * HQ 기본 토픽(hq-stock-low, hq-expire-soon)에 대해 즉시 토픽 구독/해제를 반영한다.</p>
	 *
	 * @param req       업데이트 요청 DTO
	 * @param principal 서블릿 프린시펄
	 * @param auth      스프링 인증 객체
	 * @return 저장된 Preference ID 및 ok 플래그
	 */
	@Operation(summary = "나의 수신 설정 업데이트", description = "로그인 사용자의 FCM 수신 설정을 저장하고 토픽 구독을 반영할 수 있습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "업데이트 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청"),
			@ApiResponse(responseCode = "403", description = "권한 없음")
	})
	@PostMapping("/me")
	public ResponseEntity<Map<String, Object>> updateMine(@Valid @RequestBody FcmPreferenceUpdateRequest req,
														  Principal principal, Authentication auth) {
		Long memberId = extractMemberId(principal, auth);
		FcmPreference saved = prefService.upsertForHqMember(
				memberId, req.catNotice(), req.catStockLow(), req.catExpireSoon(), req.thresholdDays());

		boolean apply = req.applySubscriptions() == null || Boolean.TRUE.equals(req.applySubscriptions());
		if (apply) {
			if (req.catStockLow() != null) {
				if (req.catStockLow()) fcmService.subscribeToTopic("hq-stock-low", memberId);
				else fcmService.unsubscribeFromTopic("hq-stock-low", memberId);
			}
			if (req.catExpireSoon() != null) {
				if (req.catExpireSoon()) fcmService.subscribeToTopic("hq-expire-soon", memberId);
				else fcmService.unsubscribeFromTopic("hq-expire-soon", memberId);
			}
		}
		return ResponseEntity.ok(Map.of("ok", true, "prefId", saved.getFcmPreferenceId()));
	}

	/**
	 * 세션/인증 객체에서 Member ID를 안전하게 추출한다.
	 *
	 * @param principal 서블릿 프린시펄
	 * @param auth      스프링 인증 객체
	 * @return memberId
	 * @throws IllegalStateException 식별 불가 시 예외 발생
	 */
	private Long extractMemberId(Principal principal, Authentication auth) {
		var authentication = SecurityContextHolder.getContext().getAuthentication();
		Object p = (auth != null && auth.getPrincipal() != null)
				? auth.getPrincipal()
				: (authentication != null ? authentication.getPrincipal() : null);

		if (p instanceof Member m) return m.getId();

		if (p instanceof UserDetails ud) {
			return myPageService.findByEmail(ud.getUsername()).getId();
		}

		if (principal != null && principal.getName() != null && !principal.getName().isBlank()) {
			return myPageService.findByEmail(principal.getName()).getId();
		}

		throw new IllegalStateException("로그인 사용자를 식별할 수 없습니다.");
	}
}
