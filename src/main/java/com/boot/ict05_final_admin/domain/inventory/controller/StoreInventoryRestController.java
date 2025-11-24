package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.service.StoreInventoryService;
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
 * 가맹점 재고 REST 컨트롤러.
 *
 * <p>제공 기능:</p>
 * <ul>
 *   <li>가맹점 재고 목록 조회(JSON, 페이징)</li>
 *   <li>가맹점 재고 목록 엑셀 다운로드</li>
 * </ul>
 *
 * <p>도메인 규칙/트랜잭션은 서비스 계층에서 처리한다.</p>
 *
 * @author 김주연
 * @since 2025.11.12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API/store/inventory")
public class StoreInventoryRestController {

    private final StoreInventoryService storeInventoryService;
    private final StoreNameResolver storeNameResolver;

    /**
     * 가맹점 재고 목록 조회(JSON).
     *
     * <p>검색 조건과 페이징 정보를 받아 페이지 단위로 반환한다.</p>
     *
     * @param searchDTO 검색 조건 DTO(storeId, 키워드/필드 등), 선택
     * @param page      페이지 번호(0-base, 기본값 0)
     * @param size      페이지 크기(기본값 10)
     * @return 가맹점 재고 목록 페이지
     */
    @GetMapping("/list")
    public Page<StoreInventoryListDTO> listStoreInventory(final StoreInventorySearchDTO searchDTO,
                                                          @RequestParam(defaultValue = "0") final int page,
                                                          @RequestParam(defaultValue = "10") final int size) {
        final Pageable pageable = PageRequest.of(page, size);
        return storeInventoryService.listStoreInventory(searchDTO, pageable);
    }

    /**
     * 가맹점 재고 목록 엑셀 다운로드.
     *
     * <p>검색 조건/정렬(페이지네이션 정보는 정렬 힌트로 활용)을 반영한 XLSX를 생성한다.
     * {@code storeId}가 지정되면 해당 가맹점명으로 파일명을 구성하고, 미지정 시 전체 기준 파일명을 사용한다.</p>
     *
     * @param searchDTO 검색 조건
     * @param pageable  페이징/정렬 정보(정렬 힌트)
     * @param storeId   가맹점 ID(선택, null이면 전체)
     * @return XLSX 바이너리 응답(적절한 Content-Disposition 포함)
     * @throws IOException 워크북 생성/쓰기 중 I/O 오류
     */
    @GetMapping("/download")
    @Operation(summary = "가맹점 재고 목록 엑셀 다운로드")
    public ResponseEntity<byte[]> downloadStoreInventory(final StoreInventorySearchDTO searchDTO,
                                                         final Pageable pageable,
                                                         @RequestParam(required = false) final Long storeId)
            throws IOException {
        byte[] xlsx = storeInventoryService.downloadExcel(searchDTO, pageable);
        String storeName = (storeId == null) ? null : storeNameResolver.resolveOrFallback(storeId);
        return ExcelResponse.ok(xlsx, ExcelFilename.storeInventory(storeName));
    }
}
