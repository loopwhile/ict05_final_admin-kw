package com.boot.ict05_final_admin.domain.nav.controller;

import com.boot.ict05_final_admin.domain.nav.service.NavGateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 시스템 메뉴 권한 상태 변경용 REST 컨트롤러
 *
 * 역할
 * - 단일 메뉴 항목의 활성화 상태를 on, off, toggle 한다
 * - 화면에서는 버튼 클릭 시 비동기 호출로 사용한다
 *
 * 보안
 * - 관리자 전용 엔드포인트로 가정한다
 * - Security 설정에서 적절한 권한 체크가 필요하다
 *
 * 응답 규약
 * - 성공 시 200과 JSON { success: true, enabled: 현재상태 } 를 반환한다
 * - 실패 시 서비스 계층에서 던지는 예외를 전역 예외 처리로 변환하여 상태 코드를 통일한다
 *
 * 기타
 * - enable, disable 는 효과 기준으로는 멱등이다
 * - toggle 은 멱등이 아니므로 반복 호출 시 상태가 매번 바뀐다
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/navauth")
public class NavauthRestController {

    private final NavGateService navGateService;

    /**
     * 메뉴 활성화
     *
     * 경로
     * - POST /api/navauth/{id}/enable
     *
     * 파라미터
     * - id 대상 메뉴의 식별자
     *
     * 반환
     * - enabled true 로 설정된 최종 상태를 담아 반환한다
     *
     * 예외 처리
     * - 존재하지 않는 id, 권한 없음 등은 전역 예외 처리기에서 404 또는 403 등으로 변환한다
     */
    @PostMapping("/{id}/enable")
    public ResponseEntity<?> enable(@PathVariable Long id) {
        boolean enabled = navGateService.setEnabled(id, true);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enabled));
    }

    /**
     * 메뉴 비활성화
     *
     * 경로
     * - POST /api/navauth/{id}/disable
     *
     * 파라미터
     * - id 대상 메뉴의 식별자
     *
     * 반환
     * - enabled false 로 설정된 최종 상태를 담아 반환한다
     */
    @PostMapping("/{id}/disable")
    public ResponseEntity<?> disable(@PathVariable Long id) {
        boolean enabled = navGateService.setEnabled(id, false);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enabled));
    }

    /**
     * 메뉴 활성화 상태 토글
     *
     * 경로
     * - POST /api/navauth/{id}/toggle
     *
     * 설명
     * - 현재 상태를 반전시킨다
     * - 연속 호출 시 상태가 번갈아 바뀌므로 주의한다
     *
     * 반환
     * - 토글 이후의 enabled 상태를 담아 반환한다
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<?> toggle(@PathVariable Long id) {
        boolean enabled = navGateService.toggle(id);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enabled));
    }
}
