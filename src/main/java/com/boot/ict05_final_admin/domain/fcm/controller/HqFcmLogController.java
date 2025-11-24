package com.boot.ict05_final_admin.domain.fcm.controller;

import com.boot.ict05_final_admin.domain.fcm.repository.FcmSendLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FCM 전송 로그 뷰 컨트롤러.
 *
 * <p>최근 전송 로그를 조회하여 서버 사이드 템플릿(fcm/logs) 렌더링에 필요한 모델을 준비한다.
 * 관리 화면용(Thymeleaf)으로 동작한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Controller
@RequestMapping("/fcm/logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
@Tag(name = "FCM 전송 로그", description = "본사용 FCM 전송 로그 뷰")
public class HqFcmLogController {

	private final FcmSendLogRepository logRepo;

	/**
	 * 최근 전송 로그 목록 페이지.
	 *
	 * @param limit 조회할 최대 행 개수(1~500 사이로 안전하게 정제)
	 * @param model 템플릿 모델
	 * @return 뷰 템플릿 경로 "fcm/logs"
	 */
	@Operation(summary = "전송 로그 목록(뷰)", description = "최근 FCM 전송 로그를 조회하여 로그 페이지를 렌더링합니다.")
	@GetMapping
	public String logs(@RequestParam(defaultValue = "100") int limit, Model model) {
		int safe = Math.max(1, Math.min(500, limit));
		model.addAttribute("rows", logRepo.findRecent(safe));
		model.addAttribute("limit", safe);
		return "fcm/logs";
	}
}
