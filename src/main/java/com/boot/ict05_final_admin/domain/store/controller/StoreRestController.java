package com.boot.ict05_final_admin.domain.store.controller;

import com.boot.ict05_final_admin.domain.store.dto.StoreModifyFormDTO;
import com.boot.ict05_final_admin.domain.store.dto.StoreWriteFormDTO;
import com.boot.ict05_final_admin.domain.store.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 가맹점 등록/수정 API를 제공하는 REST 컨트롤러입니다.
 *
 * <p>본사 관리자에서 사용하는 가맹점 등록 및 수정 API를 담당합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(
        name = "가맹점 API",
        description = "가맹점 등록/조회/수정 기능 제공"
)
@Slf4j
public class StoreRestController {

    private final StoreService storeService;

    /**
     * 가맹점을 등록하는 API입니다.
     *
     * <p>multipart/form-data 형식의 폼 데이터를 받아 유효성 검증 후,
     * 가맹점 정보를 저장합니다.</p>
     *
     * @param dto           가맹점 등록 정보 DTO
     * @param bindingResult 유효성 검증 결과
     * @return 처리 결과(JSON, success/id or errors/message)
     */
    @PostMapping(
            value = "/store/write",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "가맹점 등록",
            description = "본사에서 새로운 가맹점을 등록하는 API입니다. " +
                    "multipart/form-data로 StoreWriteFormDTO를 전송해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가맹점 등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력 값 유효성 검증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Map<String, Object>> addOfficeStaff(
            @Parameter(
                    description = "가맹점 등록 폼 데이터",
                    required = true
            )
            @Validated @ModelAttribute StoreWriteFormDTO dto,
            BindingResult bindingResult
    ) {
        // DTO 바인딩 확인 로그
        log.info("WRITE DTO = {}", dto);

        // 유효성 검증 실패 시
        if (bindingResult.hasErrors()) {
            log.warn("VALIDATION ERRORS: {}", bindingResult.getFieldErrors());

            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fe -> fe.getField(),
                            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : fe.getCode(),
                            (a, b) -> a // 같은 필드 에러가 여러 개면 첫 번째 걸로 유지
                    ));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        try {
            Long id = storeService.insertOfficeStore(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", id
            ));
        } catch (Exception e) {
            log.error("STORE WRITE FAILED", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "서버에서 등록 처리 중 오류가 발생했습니다."
                    ));
        }
    }


    /**
     * 기존 가맹점을 수정하는 API입니다.
     *
     * <p>multipart/form-data 형식의 폼 데이터를 받아 유효성 검증 후,
     * 해당 가맹점 정보를 수정합니다.</p>
     *
     * @param dto           수정할 가맹점 정보 DTO
     * @param bindingResult 유효성 검증 결과
     * @return 처리 결과(JSON, success/id or errors/message)
     */
    @PostMapping(
            value = "/store/modify",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
            summary = "가맹점 수정",
            description = "기존 가맹점을 수정하는 API입니다. " +
                    "multipart/form-data로 StoreModifyFormDTO를 전송해야 합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "가맹점 수정 성공"),
            @ApiResponse(responseCode = "400", description = "입력 값 유효성 검증 실패"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<Map<String, Object>> modifyStore(
            @Parameter(
                    description = "수정할 가맹점 정보 폼 데이터",
                    required = true
            )
            @Valid @ModelAttribute StoreModifyFormDTO dto,
            BindingResult bindingResult
    ) {
        // DTO 바인딩 확인 로그
        log.info("MODIFY DTO = {}", dto);

        // 유효성 검증 실패 시
        if (bindingResult.hasErrors()) {
            log.warn("VALIDATION ERRORS(MODIFY): {}", bindingResult.getFieldErrors());

            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fe -> fe.getField(),
                            fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : fe.getCode(),
                            (a, b) -> a
                    ));

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        try {
            Long id = storeService.storeModify(dto).getId();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", id
            ));
        } catch (Exception e) {
            log.error("STORE MODIFY FAILED", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "서버에서 수정 처리 중 오류가 발생했습니다."
                    ));
        }
    }
}
