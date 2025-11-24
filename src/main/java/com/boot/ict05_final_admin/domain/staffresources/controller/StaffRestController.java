package com.boot.ict05_final_admin.domain.staffresources.controller;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffModifyFormDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffWriteFormDTO;
import com.boot.ict05_final_admin.domain.staffresources.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 인사 급여 도메인의 REST 컨트롤러
 *
 * 사원 등록 조회 수정 삭제에 관한 API 엔드포인트를 제공한다
 * 성공 응답은 success 키를 중심으로 반환하며 검증 오류 시 400과 함께 errors 맵을 전달한다
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "인사/급여 API", description = "사원 등록/조회/수정/삭제 기능 제공")
@Slf4j
public class StaffRestController {

    private final StaffService staffService;

    /**
     * 사원 등록 API
     *
     * @param dto 등록할 사원 데이터
     * @param bindingResult 유효성 검증 결과
     * @return 등록 성공 여부 및 생성된 재료 ID
     * @throws Exception 파일 업로드 실패 시 예외 발생 가능
     */
    @PostMapping("/staff/write")
    @Operation(
            summary = "신규 사원 등록",
            description = "본사에서 새로운 사원을 등록하는 API입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사원 등록 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "등록 성공",
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
    public ResponseEntity<Map<String, Object>> addOfficeStaff(
            @Validated @ModelAttribute StaffWriteFormDTO dto,
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

        Long id = staffService.insertOfficeStaff(dto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }

    /**
     * 사원 수정 API
     *
     * <p>기존 사원를 수정하는 엔드포인트입니다.</p>
     *
     * @param dto 수정할 사원 데이터 (이름, 단위, 카테고리, 상태, 첨부파일 등)
     * @param bindingResult 유효성 검증 결과
     * @return 수정 성공 여부 및 재료 ID
     * @throws Exception 파일 업로드 실패 시 예외 발생 가능
     */
    @PostMapping("/staff/modify")
    @Operation(
            summary = "사원 수정",
            description = "기존 사원 정보를 수정하는 API입니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사원 수정 정보",
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
    public ResponseEntity<Map<String, Object>> modifyStaff(
            @Valid @ModelAttribute StaffModifyFormDTO dto,
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

        Long id = staffService.staffModify(dto).getId();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }

    /**
     * 사원 삭제 API
     *
     * @param id 사원 ID
     * @return 삭제 성공 여부
     */
    @DeleteMapping("/staff/delete")
    @Operation(summary = "사원 삭제", description = "사원 ID를 기준으로 사원 정보를 삭제합니다.")
    public ResponseEntity<Map<String, Object>> deleteStaff(@RequestParam("id") Long id) {
        staffService.deleteStaff(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of("success", true, "id", id));
    }
}
