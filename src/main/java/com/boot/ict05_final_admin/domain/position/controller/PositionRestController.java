package com.boot.ict05_final_admin.domain.position.controller;

import com.boot.ict05_final_admin.domain.position.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 직책/직무(포지션) 및 회원-사원 연동을 담당하는 REST 컨트롤러.
 *
 * 본사에서 특정 회원(Member)을 매장 사원(Staff)과 연결(link)하는 등
 * 상태 변경성 API를 제공한다. 실제 비즈니스 로직은 {@link PositionService}에서 수행한다.
 */
@RestController
@RequestMapping("/API")
@RequiredArgsConstructor
public class PositionRestController {
    private final PositionService positionService;

    /**
     * 회원과 사원을 연동한다.
     *
     * <p>전제: memberId가 유효하고, 해당 회원/사원 상태가 연동 가능해야 한다.
     * 연동 완료 후 연동된 엔티티의 식별자를 반환한다.</p>
     *
     * <p>권장: 운영 환경에서는 권한 체크(예: hasRole('HQ'))와
     * 입력 검증/예외 매핑을 명확히 해줄 것.</p>
     *
     * @param memberId 연동할 회원 식별자
     * @return {"success": true, "id": <연동된 엔티티 ID>} 형태의 JSON 응답
     */
    @PostMapping("/member/{memberId}/linkStaff")
    @Operation(
            summary = "사원 연동",
            description = "본사에서 회원과 사원을 연동하는 기능입니다",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사원 연동 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "연동 성공",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json"
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "검증 오류 발생"
                    )
            }
    )
    public ResponseEntity<?> memberLinkStaff(@PathVariable Long memberId) {
        Long id = positionService.memberLinkStaff(memberId).getId();
        return ResponseEntity.ok(Map.of("success", true, "id", id));
    }
}
