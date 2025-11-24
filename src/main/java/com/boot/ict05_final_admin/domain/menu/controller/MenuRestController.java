package com.boot.ict05_final_admin.domain.menu.controller;

import com.boot.ict05_final_admin.domain.menu.dto.MenuModifyFormDTO;
import com.boot.ict05_final_admin.domain.menu.dto.MenuWriteFormDTO;
import com.boot.ict05_final_admin.domain.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 메뉴 관련 REST API 컨트롤러.
 *
 * <p>메뉴 등록 및 수정 기능을 제공한다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "메뉴 API", description = "메뉴 등록/수정 API")
@Slf4j
public class MenuRestController {

    private final MenuService menuService;

    /**
     * 메뉴 등록.
     *
     * <p>멀티파트 폼 데이터로 전달된 메뉴 정보를 저장한다.</p>
     *
     * @param dto            등록 폼 DTO
     * @param bindingResult  검증 결과
     * @return 등록 결과(JSON)
     */
    @PostMapping(value = "/menu/write", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "메뉴 등록",
            description = "멀티파트 폼 데이터로 전달된 메뉴 정보를 저장한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "검증 오류",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, Object>> addStoreMenu(
            @Validated @ModelAttribute MenuWriteFormDTO dto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            e -> e.getField(),
                            e -> e.getDefaultMessage(),
                            (a, b) -> a
                    ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "errors", errors));
        }

        Long menuId = menuService.insertStoreMenu(dto);
        return ResponseEntity.ok(Map.of("success", true, "menuId", menuId));
    }

    /**
     * 메뉴 수정.
     *
     * <p>기존 메뉴의 정보를 수정한다.</p>
     *
     * @param menuId         메뉴 ID
     * @param dto            수정 폼 DTO
     * @param br             검증 결과
     * @return 수정 결과(JSON)
     */
    @PostMapping(value = "/menu/modify/{menuId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            summary = "메뉴 수정",
            description = "기존 메뉴의 정보를 수정한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "400", description = "검증 오류",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "중복 충돌(예: 메뉴 코드 중복)",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Map<String, Object>> modifyMenu(
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @ModelAttribute @Valid MenuModifyFormDTO dto,
            BindingResult br) {

        dto.setMenuId(menuId);
        if (dto.getMainMaterials() == null) dto.setMainMaterials(new ArrayList<>());
        if (dto.getSauceMaterials() == null) dto.setSauceMaterials(new ArrayList<>());

        if (br.hasErrors()) {
            Map<String, String> errors = br.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            e -> e.getField(),
                            e -> e.getDefaultMessage(),
                            (a, b) -> a
                    ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "errors", errors));
        }

        try {
            menuService.menuModify(dto);
            return ResponseEntity.ok(Map.of("success", true, "id", menuId));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "success", false,
                            "field", "menuCode",
                            "message", "이미 사용 중인 상품코드입니다."
                    ));
        }
    }

    /**
     * 메뉴 수정(간단 응답).
     *
     * <p>동일 경로의 간단 응답 버전이다. Accept 헤더에 따라 라우팅될 수 있다.</p>
     *
     * @param menuId 메뉴 ID
     * @param dto    수정 폼 DTO
     * @return 수정 결과(JSON)
     */
    @PostMapping("/menu/modify/{menuId}")
    @Operation(
            summary = "메뉴 수정(간단 응답)",
            description = "간단한 JSON 응답을 반환하는 메뉴 수정 엔드포인트."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class)))
    })
    public Map<String, Object> modify(
            @Parameter(description = "메뉴 ID") @PathVariable Long menuId,
            @ModelAttribute MenuModifyFormDTO dto) {
        dto.setMenuId(menuId);
        menuService.menuModify(dto);
        return Map.of("success", true, "id", menuId);
    }
}
