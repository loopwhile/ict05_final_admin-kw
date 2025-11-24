package com.boot.ict05_final_admin.domain.member.controller;

import com.boot.ict05_final_admin.domain.member.dto.MemberModifyFormDTO;
import com.boot.ict05_final_admin.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 회원 관리 API 컨트롤러.
 *
 * 회원 정보 수정 등 상태 변경 요청을 처리한다.
 * 입력 검증 결과를 표준 응답 형태로 내려주며, 서비스 계층 호출과 예외 처리의 경계를 담당한다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "회원관리 API", description = "회원 조회/수정/삭제 기능 제공")
@Slf4j
public class MemberRestController {

    private final MemberService memberService;

    /**
     * 회원 수정 요청을 처리한다.
     *
     * 유효성 검증에 실패하면 400과 필드별 오류 메시지를, 성공하면 200과 수정된 회원 id를 반환한다.
     * 폼 제출 시에는 @ModelAttribute 바인딩을 통해 DTO에 값을 채운다.
     *
     * @param dto           수정할 회원 정보 DTO
     * @param bindingResult 입력값 검증 결과
     * @return 처리 결과를 담은 ResponseEntity
     * @throws Exception 서비스 계층 처리 중 발생하는 예외 전파
     */
    @PostMapping("/member/modify")
    @Operation(
            summary = "회원 수정",
            description = "기존 회원 정보를 수정하는 API입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "회원 수정 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
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
    public ResponseEntity<Map<String, Object>> modifyMember(
            @Valid @ModelAttribute MemberModifyFormDTO dto,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        Long id = memberService.memberModify(dto).getId();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }
}
