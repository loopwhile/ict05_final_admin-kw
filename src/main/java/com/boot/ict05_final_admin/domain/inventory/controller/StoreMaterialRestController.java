package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.service.StoreMaterialService;
import com.boot.ict05_final_admin.domain.inventory.service.StoreNameResolver;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelFilename;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 가맹점 재료 REST API 컨트롤러.
 *
 * <p>제공 기능:</p>
 * <ul>
 *   <li>가맹점 재료 목록 조회(JSON, 페이징)</li>
 *   <li>가맹점 재료 목록 엑셀 다운로드</li>
 * </ul>
 *
 * <p>도메인 검증/트랜잭션 로직은 서비스 계층에서 처리한다.</p>
 *
 * @author 김주연
 * @since 2025.10.22
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API/store/material")
public class StoreMaterialRestController {

    private final StoreMaterialService storeMaterialService;
    private final StoreNameResolver storeNameResolver;

    /**
     * 가맹점 재료 목록 조회(JSON).
     *
     * <p>검색 조건과 페이징 정보를 받아 페이지 단위로 반환한다.</p>
     *
     * @param searchDTO 검색 조건 DTO(storeId, 키워드, 검색필드 등), 선택
     * @param page      페이지 번호(0-base, 기본값 0)
     * @param size      페이지 크기(기본값 10)
     * @return 가맹점 재료 목록 페이지
     */
    @GetMapping("/list")
    public Page<StoreMaterialListDTO> listStoreMaterials(final StoreMaterialSearchDTO searchDTO,
                                                         @RequestParam(defaultValue = "0") final int page,
                                                         @RequestParam(defaultValue = "10") final int size) {
        Pageable pageable = PageRequest.of(page, size);
        return storeMaterialService.listStoreMaterials(searchDTO, pageable);
    }

    /**
     * 가맹점 재료 목록 엑셀 다운로드.
     *
     * <p>검색 조건/정렬(페이지네이션 정보는 정렬 힌트로 활용)을 반영해 XLSX를 생성한다.
     * {@code storeId}가 지정되면 해당 가맹점명으로 파일명을 구성하고, 미지정 시 전체 기준 파일명을 사용한다.</p>
     *
     * @param searchDTO 검색 조건
     * @param pageable  페이징/정렬 정보(정렬 힌트)
     * @param storeId   가맹점 ID(선택, null이면 전체)
     * @return XLSX 바이너리 응답(적절한 Content-Disposition 포함)
     * @throws IOException 워크북 생성/쓰기 중 I/O 오류
     */
    @GetMapping("/download")
    @Operation(summary = "가맹점 재료 목록 엑셀 다운로드")
    public ResponseEntity<byte[]> downloadStoreMaterial(final StoreMaterialSearchDTO searchDTO,
                                                        final Pageable pageable,
                                                        @RequestParam(required = false) final Long storeId)
            throws IOException {
        byte[] xlsx = storeMaterialService.downloadExcel(searchDTO, pageable);
        String storeName = (storeId == null) ? null : storeNameResolver.resolveOrFallback(storeId);
        return ExcelResponse.ok(xlsx, ExcelFilename.storeMaterial(storeName));
    }
}
