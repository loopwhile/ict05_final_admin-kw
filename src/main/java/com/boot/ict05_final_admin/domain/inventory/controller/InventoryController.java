package com.boot.ict05_final_admin.domain.inventory.controller;

import com.boot.ict05_final_admin.domain.inventory.dto.*;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.service.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 조회/삭제 전용 컨트롤러.
 *
 * <p>
 * - SSR 페이지 라우팅(Thymeleaf)<br>
 * - 조회용 GET JSON API<br>
 * - 삭제 전용 DELETE API
 * </p>
 *
 * <p>트랜잭션은 서비스 계층에서 처리한다.</p>
 *
 * @author 김주연
 * @since 2025.10.23
 */
@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final MaterialService materialService;
    private final InventoryService inventoryService;
    private final InventoryInService inventoryInService;
    private final InventoryOutService inventoryOutService;
    private final InventoryLotService inventoryLotService;
    private final InventoryLogViewService inventoryLogViewService;
    private final InventoryBatchService inventoryBatchService;
    private final InventoryAdjustmentService inventoryAdjustmentService;

    // -------------------- View routing --------------------

    /**
     * 본사 재고 목록(SSR)을 페이징으로 조회한다.
     *
     * <p>검색 조건과 페이징 정보를 받아 서버 사이드 렌더링으로 목록을 반환한다.</p>
     *
     * @param inventorySearchDTO 검색 조건 DTO(재료명, 상태 등)
     * @param pageable           페이징 정보(페이지 번호, 크기, 정렬 기준). 1-base 페이지 인덱스를 사용한다.
     * @param model              뷰 모델
     * @param request            현재 요청(페이지네이션 링크 생성을 위해 사용)
     * @return 재고 목록 템플릿 경로
     */
    @GetMapping("/list")
    public String listInventory(InventorySearchDTO inventorySearchDTO,
                                @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                Model model,
                                HttpServletRequest request) {

        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize());
        Page<InventoryListDTO> inventories = inventoryService.listInventory(inventorySearchDTO, pageRequest);

        model.addAttribute("inventories", inventories);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("inventorySearchDTO", inventorySearchDTO);

        return "inventory/list";
    }

    /**
     * 로그 화면의 fragments/pagination 이 기대하는 {@code urlBuilder} 변수를 주입하기 위한 헬퍼.
     */
    static final class UrlBuilderHelper {
        public ServletUriComponentsBuilder fromCurrentRequest() {
            return ServletUriComponentsBuilder.fromCurrentRequest();
        }
    }

    /**
     * 본사 재고 로그 화면(SSR)으로 이동한다.
     *
     * @param materialId 재료 ID(필수)
     * @param type       로그 유형 필터(INCOME/OUTGO/ADJUST 등), 선택
     * @param startDate  시작일(선택, ISO yyyy-MM-dd)
     * @param endDate    종료일(선택, ISO yyyy-MM-dd)
     * @param page       페이지 인덱스(0-base)
     * @param size       페이지 크기
     * @param model      뷰 모델
     * @return 재고 로그 템플릿 경로
     */
    @GetMapping("/log/{materialId}")
    public String logPage(@PathVariable Long materialId,
                          @RequestParam(required = false) String type,
                          @RequestParam(required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam(required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          Model model) {

        // 재료
        var material = materialService.findById(materialId);
        String materialName = material != null ? material.getName() : "";

        // 재고: Optional → 실제 엔티티(or null)로 변환
        var inventoryOpt = inventoryService.findByMaterialId(materialId); // Optional<Inventory>
        var inventory = inventoryOpt != null ? inventoryOpt.orElse(null) : null;

        // 로그 페이징
        Page<InventoryLogDTO> logs = inventoryLogViewService.getFilteredLogs(
                materialId, type, startDate, endDate, PageRequest.of(page, size));

        // 뷰 모델
        model.addAttribute("logs", logs);
        model.addAttribute("materialId", materialId);
        model.addAttribute("materialName", materialName);
        model.addAttribute("material", material);
        model.addAttribute("inventory", inventory);
        model.addAttribute("selectedType", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("urlBuilder", new UrlBuilderHelper());

        return "inventory/log";
    }

    /**
     * 본사 재고 입고 등록 화면(SSR).
     *
     * <p>입고 대상 재료 선택 및 수량/단가 입력 폼을 렌더링한다.</p>
     *
     * @param model 뷰 모델
     * @return 입고 등록 템플릿 경로
     */
    @GetMapping("/in/write")
    public String showInventoryInForm(Model model) {
        model.addAttribute("categories", MaterialCategory.values());
        model.addAttribute("inventoryList", inventoryService.findAllForSelect());
        model.addAttribute("now", LocalDateTime.now());
        return "inventory/inventory_in_write";
    }

    /**
     * 본사 출고 테스트 화면(SSR).
     *
     * @param model 뷰 모델
     * @return 출고 테스트 템플릿 경로
     */
    @GetMapping("/out_test")
    public String outTestPage(Model model) {
        model.addAttribute("categories", MaterialCategory.values());
        model.addAttribute("inventoryList", inventoryService.findAllForSelect());
        model.addAttribute("now", LocalDateTime.now());
        return "inventory/out_test";
    }

    /**
     * 재료별 배치 현황 화면(SSR).
     *
     * @param materialId 재료 ID
     * @param model      뷰 모델
     * @return 배치 현황 템플릿 경로
     */
    @GetMapping("/batch-status/{materialId}")
    public String batchStatusPage(@PathVariable Long materialId, Model model) {
        model.addAttribute("batches", inventoryBatchService.getBatchesByMaterial(materialId));
        model.addAttribute("materialId", materialId);
        model.addAttribute("materialName", materialService.findById(materialId).getName());
        return "inventory/batch_status";
    }

    /**
     * 특정 배치(LOT)의 입고/출고 상세 화면(SSR).
     *
     * @param batchId 배치 ID(inventory_batch.inventory_batch_id)
     * @param page    출고 이력 페이지 인덱스(0-base)
     * @param size    페이지 크기
     * @param model   뷰 모델
     * @return 배치 상세 템플릿 경로
     */
    @GetMapping("/batch/{batchId}")
    public String batchPage(@PathVariable Long batchId,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {

        // LOT 상단 상세
        InventoryLotDetailDTO lot = inventoryBatchService.getLotDetail(batchId);

        // 출고 이력 페이징
        Page<InventoryOutLotHistoryRowDTO> outHistory =
                inventoryLotService.getOutLotHistory(batchId, PageRequest.of(page, size));

        model.addAttribute("lot", lot);
        model.addAttribute("outHistory", outHistory);

        model.addAttribute("batchId", batchId);
        model.addAttribute("materialId", lot.getMaterialId());
        model.addAttribute("materialName", lot.getMaterialName());

        // pagination fragment용
        model.addAttribute("urlBuilder", new UrlBuilderHelper());

        return "inventory/batch";
    }

    // -------------------- Read APIs (JSON) --------------------

    /**
     * 재료별 배치 현황(LOT) 목록을 조회한다.
     *
     * @param materialId 재료 ID
     * @return 배치 현황 행 리스트
     */
    @GetMapping("/lot/batch-status")
    @ResponseBody
    public List<BatchStatusRowDTO> batchStatus(@RequestParam Long materialId) {
        return inventoryLotService.getBatchStatusForMaterial(materialId)
                .stream()
                .map(r -> BatchStatusRowDTO.builder()
                        .batchId(r.getBatchId())
                        .lotNo(r.getLotNo())
                        .receivedDate(r.getReceivedDate())
                        .expirationDate(r.getExpirationDate())
                        .receivedQty(r.getReceivedQty())
                        .remainQty(r.getRemainQty())
                        .unitPrice(r.getUnitPrice())
                        .build())
                .toList();
    }

    /**
     * 특정 배치의 출고 이력(JSON)을 페이징 조회한다.
     *
     * @param batchId 배치 ID
     * @param page    페이지 번호(0-base)
     * @param size    페이지 크기
     * @return 출고 이력 페이지
     */
    @GetMapping("/lot/{batchId}/out-history")
    @ResponseBody
    public Page<InventoryOutLotHistoryRowDTO> batchOutHistory(@PathVariable Long batchId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return inventoryLotService.getOutLotHistory(batchId, PageRequest.of(page, size));
    }

    /**
     * 본사 재고 로그(JSON)를 필터로 페이징 조회한다.
     *
     * @param materialId 재료 ID
     * @param type       구분(입고/출고/조정) 문자열, null 가능
     * @param startDate  시작일(yyyy-MM-dd), null 가능
     * @param endDate    종료일(yyyy-MM-dd), null 가능
     * @param page       페이지 번호(0-base)
     * @param size       페이지 크기
     * @return 로그 페이지
     */
    @GetMapping("/logs")
    @ResponseBody
    public Page<InventoryLogDTO> logs(@RequestParam Long materialId,
                                      @RequestParam(required = false) String type,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                      @RequestParam(required = false)
                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {

        return inventoryLogViewService.getFilteredLogs(
                materialId,
                type,
                startDate,
                endDate,
                PageRequest.of(page, size)
        );
    }

    /**
     * 조정 상세(JSON). 로그 팝업에서 사용.
     *
     * @param logId 재고 조정 로그 ID
     * @return 200 OK: {@link InventoryAdjustDTO}, 404 NOT_FOUND: 오류 메시지
     */
    @GetMapping("/log/adjust/{logId}")
    @ResponseBody
    public ResponseEntity<?> getAdjustDetail(@PathVariable Long logId) {
        InventoryAdjustDTO dto = inventoryAdjustmentService.getAdjustDetail(logId);

        if (dto == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message", "조정 로그를 찾을 수 없습니다.",
                            "logId", logId
                    ));
        }

        return ResponseEntity.ok(dto);
    }

    /**
     * LOT 상세(JSON). 로그 팝업에서 사용.
     *
     * @param batchId 배치 ID
     * @return LOT 상세 DTO
     */
    @GetMapping("/log/lot/{batchId}")
    @ResponseBody
    public InventoryLotDetailDTO getLotDetail(@PathVariable Long batchId) {
        return inventoryBatchService.getLotDetail(batchId);
    }

    /**
     * 출고 LOT 상세(JSON). 로그 팝업에서 사용.
     *
     * @param outId 출고 헤더 ID
     * @return 출고 LOT 상세 행 리스트
     */
    @GetMapping("/log/out/{outId}")
    @ResponseBody
    public List<InventoryOutLotDetailRowDTO> getOutDetail(@PathVariable Long outId) {
        return inventoryLotService.getOutDetailByOutId(outId);
    }

    // -------------------- Delete APIs --------------------

    /**
     * 출고 헤더 삭제.
     *
     * @param outId 출고 ID
     */
    @DeleteMapping("/out/{outId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOut(@PathVariable Long outId) {
        inventoryOutService.deleteOut(outId);
    }

    /**
     * 입고 헤더 삭제.
     *
     * @param inId 입고 ID
     */
    @DeleteMapping("/in/{inId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIn(@PathVariable Long inId) {
        inventoryInService.deleteIn(inId);
    }

    /**
     * 출고-로트 아이템 삭제.
     *
     * @param lotId 출고-로트 아이템 ID
     */
    @DeleteMapping("/lot/out-item/{lotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOutLotItem(@PathVariable Long lotId) {
        inventoryLotService.deleteOutLot(lotId);
    }
}
