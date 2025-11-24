package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.*;
import com.boot.ict05_final_admin.domain.inventory.service.*;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelFilename;
import com.boot.ict05_final_admin.domain.inventory.utility.ExcelResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 본사 재고 관련 REST API 컨트롤러.
 *
 * <p>제공 기능:</p>
 * <ul>
 *   <li>출고 미리보기/확정(FIFO)</li>
 *   <li>입고 등록(단가 보정 및 배치 생성 포함)</li>
 *   <li>재고 조정 등록</li>
 *   <li>매입가(구매단가) 등록/수정</li>
 *   <li>재고/로그/배치/LOT 출고이력 엑셀 다운로드</li>
 * </ul>
 *
 * <p>트랜잭션 및 도메인 규칙은 서비스 계층에서 처리한다.</p>
 *
 * @author 김주연
 * @since 2025.11.12
 */
@Slf4j
@RestController
@RequestMapping("/API")
@RequiredArgsConstructor
public class InventoryRestController {

    private final InventoryService inventoryService;
    private final InventoryInService inventoryInService;
    private final InventoryOutService inventoryOutService;
    private final InventoryBatchService inventoryBatchService;
    private final InventoryAdjustmentService inventoryAdjustmentService;
    private final UnitPriceService unitPriceService;
    private final MaterialService materialService;

    // -------------------- Out --------------------

    /**
     * 출고 미리보기를 수행한다(FIFO).
     *
     * <p>요청 수량에 대해 현재고를 검증한 뒤, FIFO 규칙으로 배치(LOT) 분할 계획을 계산해 반환한다.</p>
     *
     * @param materialId 재료 ID(필수)
     * @param qty        총 출고 수량(DECIMAL(15,3) 스케일 준수)
     * @return 200 OK: {@code List<InventoryOutPreviewItemDTO>} 계획,
     *         400 BAD REQUEST: 수량 부족 메시지 문자열
     */
    @GetMapping("/inventory/out/preview")
    public ResponseEntity<?> previewOut(@RequestParam Long materialId, @RequestParam BigDecimal qty) {
        // 현재 재고 조회
        BigDecimal currentStock = inventoryService.hqRemainOfMaterial(materialId);
        if (currentStock == null) currentStock = BigDecimal.ZERO;

        // 주문 수량과 비교
        if (currentStock.compareTo(qty) < 0) {
            return ResponseEntity.status(400)
                    .body("주문 수량이 현재 재고를 초과합니다. 현재 재고: " + currentStock + ", 주문 수량: " + qty);
        }

        // FIFO 미리보기 처리
        List<InventoryOutPreviewItemDTO> plan = inventoryOutService.previewFifo(materialId, qty);
        return ResponseEntity.ok(plan);
    }

    /**
     * 출고를 확정한다(배치 할당 포함, FIFO).
     *
     * <p>미리보기와 동일한 FIFO 규칙으로 배치를 확정하고, 출고 헤더/아이템 및 LOT 연결을 생성한다.</p>
     *
     * @param req 출고 확정 요청 DTO
     * @return 생성된 출고 헤더 ID
     */
    @PostMapping("/inventory/out/confirm")
    public Long confirmOut(@RequestBody InventoryOutConfirmRequest req) {
        // 서비스가 DTO 오버로드를 제공하지 않으면 5파라미터 시그니처로 위임
        return inventoryOutService.confirmOut(
                req.getMaterialId(),
                req.getStoreId(),
                req.getTotalQty(),
                req.getOutDate(),
                req.getMemo()
        );
    }

    // -------------------- In --------------------

    /**
     * 본사 입고를 등록한다.
     *
     * <p>단가가 비어 있으면 최신 매입가로 보정하며, 배치를 자동 생성한다.
     * 입고 기록과 함께 단가 이력 정책은 서비스 계층 규칙을 따른다.</p>
     *
     * @param dto 입고 등록 DTO
     * @return 생성된 입고 헤더 ID
     */
    @PostMapping("/inventory/in")
    public Long insertInventoryIn(@RequestBody @Valid InventoryInWriteDTO dto) {
        return inventoryInService.insertInventoryIn(dto);
    }

    // -------------------- Adjustment --------------------

    /**
     * 본사 재고 수량 조정을 등록한다.
     *
     * <p>입출고 외 사유(분실, 파손, 오입력 등)로 재고 수량을 직접 증감한다.
     * 조정 사유/메모/발생일자 등 세부 정책은 서비스 계층에서 검증된다.</p>
     *
     * @param dto 재고 수량 조정 요청 DTO
     * @return {@code {"success": true}} 고정 응답
     */
    @PostMapping("/inventory/adjust")
    public ResponseEntity<Map<String, Object>> adjustInventory(@RequestBody InventoryAdjustDTO dto) {
        log.info("[ADJUST_CTRL] HIT dto={}", dto);
        inventoryAdjustmentService.adjustInventory(dto);
        return ResponseEntity.ok(Map.of("success", true));
    }

    // -------------------- Unit Price --------------------

    /**
     * 매입가(구매단가)를 등록한다.
     *
     * <p>특정 재료의 유효 시작 시각 기준 매입 단가를 생성한다.
     * 단가 이력 테이블 정책은 서비스 계층 규칙(중복 기간 정합성, 종료일 보정 등)을 따른다.</p>
     *
     * @param materialId 재료 ID
     * @param price      단가(DECIMAL(15,3) 스케일 준수)
     * @param validFrom  유효 시작 시각(ISO DATETIME)
     * @return 생성된 단가 ID
     */
    @PostMapping("/inventory/unit-price/purchase")
    public Long registerPurchase(@RequestParam Long materialId,
                                 @RequestParam BigDecimal price,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                 LocalDateTime validFrom) {
        return unitPriceService.setPurchasePrice(materialId, price, validFrom, null);
    }

    /**
     * 매입가(구매단가)를 수정한다.
     *
     * <p>기존 단가 레코드의 금액만 교체한다. 기간/중복 정책은 서비스 계층에서 보장한다.</p>
     *
     * @param unitPriceId 단가 ID
     * @param price       수정 단가(DECIMAL(15,3) 스케일 준수)
     * @return 수정된 단가 ID
     */
    @PutMapping("/inventory/unit-price/purchase/{unitPriceId}")
    public Long updatePurchase(@PathVariable Long unitPriceId,
                               @RequestParam BigDecimal price) {
        return unitPriceService.updatePurchasePrice(unitPriceId, price);
    }

    // -------------------- Download --------------------

    /**
     * 재고 엑셀 다운로드 API.
     *
     * <p>검색 조건과 페이징 정보를 받아 화면과 동일한 정렬/필터를 적용한 XLSX를 생성한다.</p>
     *
     * @param searchDTO 검색 조건(재료명, 상태 등)
     * @param pageable  페이징 정보(정렬 힌트로도 사용)
     * @return Excel 파일 바이너리 응답
     * @throws IOException 워크북 생성/쓰기 오류
     */
    @GetMapping("/inventory/download")
    @Operation(summary = "재고 목록 엑셀 다운로드", description = "재고 목록을 Excel 파일로 다운로드합니다.")
    public ResponseEntity<byte[]> downloadInventory(InventorySearchDTO searchDTO, Pageable pageable) throws IOException {
        byte[] xlsx = inventoryService.downloadExcel(searchDTO, pageable);
        return ExcelResponse.ok(xlsx, ExcelFilename.hqInventory());
    }

    /**
     * 재고 로그 엑셀 다운로드 API.
     *
     * <p>화면 필터(유형/기간/페이징)를 그대로 적용해 재료별 로그를 XLSX로 생성한다.
     * 서비스 내부 정책에 따라 페이징과 무관하게 전체 덤프로 생성될 수 있다.</p>
     *
     * @param materialId 재료 ID
     * @param type       로그 유형(INCOME/OUTGO/ADJUST 등), 선택
     * @param startDate  시작일(포함, ISO yyyy-MM-dd), 선택
     * @param endDate    종료일(포함, ISO yyyy-MM-dd), 선택
     * @param page       페이지 인덱스(기본 0)
     * @param size       페이지 크기(기본 10)
     * @return XLSX 바이너리 응답
     * @throws IOException 워크북 생성·쓰기 오류
     */
    @Operation(summary = "본사 재고 로그 엑셀 다운로드", description = "재료별 재고 로그를 Excel 파일로 다운로드합니다.")
    @GetMapping("/inventory/{materialId}/log/download")
    public ResponseEntity<byte[]> downloadInventoryLog(@PathVariable Long materialId,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                       @RequestParam(required = false)
                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size)
            throws IOException {
        byte[] xlsx = inventoryService.downloadLogExcel(
                materialId, type, startDate, endDate, PageRequest.of(page, size)
        );

        String materialName = Optional.ofNullable(materialService.findById(materialId))
                .map(m -> m.getName())
                .orElse(null);

        return ExcelResponse.ok(xlsx, ExcelFilename.inventoryLogByName(materialName));
    }

    /**
     * 본사 재고 배치(LOT) 엑셀 다운로드 API.
     *
     * <p>해당 재료의 전체 배치(잔량 0 포함)를 화면과 동일한 정렬 기준으로 덤프한다.
     * 서비스 내부 정책에 따라 페이지 파라미터는 정렬 힌트로만 활용될 수 있다.</p>
     *
     * @param materialId 재료 ID
     * @param page       페이지 인덱스(기본 0)
     * @param size       페이지 크기(기본 10)
     * @return XLSX 바이너리 응답
     * @throws IOException 워크북 쓰기·닫기 중 I/O 오류
     */
    @Operation(summary = "본사 재고 배치 엑셀 다운로드")
    @GetMapping("/inventory/{materialId}/batch-status/download")
    public ResponseEntity<byte[]> downloadInventoryBatch(@PathVariable Long materialId,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size)
            throws IOException {

        byte[] xlsx = inventoryService.downloadBatchExcel(materialId);

        // 재료명 조회 후 파일명 생성. 없으면 기본 규칙으로 처리
        String materialName = Optional.ofNullable(materialService.findById(materialId))
                .map(m -> m.getName())
                .orElse(null);
        String filename = ExcelFilename.inventoryBatchByName(materialName);

        return ExcelResponse.ok(xlsx, filename);
    }

    /**
     * 입고 LOT 출고 이력 엑셀 다운로드 API.
     *
     * <p>특정 배치(LOT)의 출고 이력을 엑셀로 덤프한다.</p>
     *
     * @param batchId 배치 ID
     * @return XLSX 바이너리 응답
     * @throws IOException 워크북 쓰기·닫기 중 I/O 오류
     */
    @Operation(summary = "입고 LOT 출고 이력 엑셀 다운로드")
    @GetMapping("/inventory/batch/{batchId}/out-history/download")
    public ResponseEntity<byte[]> downloadInventoryLotOutHistory(@PathVariable Long batchId)
            throws IOException {

        byte[] xlsx = inventoryService.downloadLotOutHistoryExcel(batchId);

        // LOT 번호 기반 파일명 생성: {LOT}_출고내역_YYYYMMDDHHMMSS.xlsx
        InventoryLotDetailDTO lot = inventoryBatchService.getLotDetail(batchId);
        String lotNo = lot != null ? lot.getLotNo() : null;
        String filename = ExcelFilename.inventoryLotOutHistoryByLotNo(lotNo);

        return ExcelResponse.ok(xlsx, filename);
    }
}
