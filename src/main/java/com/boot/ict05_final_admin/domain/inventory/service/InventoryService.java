package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.*;
import com.boot.ict05_final_admin.domain.inventory.entity.Inventory;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryBatch;
import com.boot.ict05_final_admin.domain.inventory.entity.InventoryLogView;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryBatchRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryLogViewRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil.n;

/**
 * 본사 재고 서비스 파사드.
 *
 * <p>
 * 재고 리스트/통계, 현재고 합산, 로그/배치/LOT 이력의 엑셀 덤프를 제공한다.
 * 화면(QueryRepository)에서 사용하는 정렬/필터 정책을 최대한 유지한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryLogViewRepository inventoryLogViewRepository;
    private final InventoryBatchRepository inventoryBatchRepository;
    private final StoreNameResolver storeNameResolver;

    private final InventoryBatchService inventoryBatchService;
    private final InventoryLotService inventoryLotService;

    /**
     * 본사 재고 목록을 페이지 단위로 조회한다.
     *
     * @param searchDTO 검색 조건 DTO
     * @param pageable  페이지/정렬 정보
     * @return 페이징 처리된 재고 리스트 DTO
     */
    @Transactional(readOnly = true)
    public Page<InventoryListDTO> listInventory(final InventorySearchDTO searchDTO,
                                                final Pageable pageable) {
        return inventoryRepository.listInventory(searchDTO, pageable);
    }

    /**
     * 본사 재고 총 건수를 조회한다.
     *
     * @param searchDTO 검색 조건 DTO
     * @return 총 건수
     */
    @Transactional(readOnly = true)
    public long countInventory(final InventorySearchDTO searchDTO) {
        return inventoryRepository.countInventory(searchDTO);
    }

    /**
     * 재료 ID로 인벤토리를 조회한다.
     *
     * @param materialId 재료 ID
     * @return 인벤토리(Optional)
     */
    @Transactional(readOnly = true)
    public Optional<Inventory> findByMaterialId(final Long materialId) {
        return inventoryRepository.findByMaterialId(materialId);
    }

    /**
     * 본사 입고 등록용 선택 목록(간단 리스트)을 조회한다.
     *
     * @return 인벤토리 엔티티 리스트
     */
    @Transactional(readOnly = true)
    public List<Inventory> findAllForSelect() {
        return inventoryRepository.findAll();
    }

    /**
     * 본사 현재고(배치 합계 기준).
     *
     * <p>해당 재료의 배치 잔량 총합을 반환한다.</p>
     *
     * @param materialId 재료 ID
     * @return 현재고(없으면 0)
     */
    @Transactional(readOnly = true)
    public BigDecimal hqRemainOfMaterial(final Long materialId) {
        return inventoryBatchRepository.findHqBatchesForMaterial(materialId).stream()
                .map(InventoryBatch::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 본사 재고 목록을 XLSX로 생성한다.
     *
     * <p>
     * 전체 건수를 조회해 단일 페이지로 일괄 조회하고, 정렬은 전달된
     * {@code pageable.getSort()}가 비어 있지 않으면 이를 반영한다.
     * </p>
     *
     * @param inventorySearchDTO 검색 조건 DTO
     * @param pageable           정렬 힌트용 Pageable(페이지/사이즈는 무시)
     * @return XLSX 바이트 배열
     * @throws IOException 워크북 쓰기/닫기 중 I/O 오류
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcel(final InventorySearchDTO inventorySearchDTO,
                                final Pageable pageable) throws IOException {
        long total = countInventory(inventorySearchDTO);
        int size = (int) Math.min(Integer.MAX_VALUE, Math.max(1, total));

        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "updateDate");

        PageRequest onePage = PageRequest.of(0, size, sort);
        Page<InventoryListDTO> page = listInventory(inventorySearchDTO, onePage);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("본사재고");
            String[] cols = {"재고ID", "재료코드", "재료명", "카테고리", "현재고", "판매단위", "상태", "최종변경일"};

            // 헤더
            Row h = sheet.createRow(0);
            CellStyle hs = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBold(true);
            hs.setFont(f);
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(hs);
            }

            // 데이터
            int r = 1;
            for (InventoryListDTO v : page) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(v.getId());
                row.createCell(1).setCellValue(v.getMaterialId());
                row.createCell(2).setCellValue(n(v.getMaterialName()));
                row.createCell(3).setCellValue(n(v.getCategoryName()));
                row.createCell(4).setCellValue(v.getQuantity() == null ? 0d : v.getQuantity().doubleValue());
                row.createCell(5).setCellValue(n(v.getMaterialSalesUnit()));
                row.createCell(6).setCellValue(String.valueOf(v.getStatus()));
                row.createCell(7).setCellValue(v.getUpdateDate() == null ? "" : v.getUpdateDate().toString());
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * 재고 로그를 XLSX로 생성한다.
     *
     * <p>
     * 조회된 로그에서 {@code storeId} 집합을 수집하고, {@link StoreNameResolver}로
     * 가맹점명을 일괄 매핑하여 기입한다.
     * </p>
     *
     * @param materialId 재료 ID
     * @param type       로그 유형 필터(옵션)
     * @param startDate  시작일(옵션)
     * @param endDate    종료일(옵션)
     * @param pageable   페이징/정렬
     * @return XLSX 바이트 배열
     * @throws IOException 워크북 쓰기/닫기 중 I/O 오류
     */
    @Transactional(readOnly = true)
    public byte[] downloadLogExcel(final Long materialId,
                                   final String type,
                                   final LocalDate startDate,
                                   final LocalDate endDate,
                                   final Pageable pageable) throws IOException {
        // 1) 로그 페이지 조회
        Page<InventoryLogView> page =
                inventoryLogViewRepository.findLogsByFilter(materialId, type, startDate, endDate, pageable);

        // 2) storeId 집합 → 이름 맵 일괄 조회
        Set<Long> ids = page.getContent().stream()
                .map(InventoryLogView::getStoreId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> nameMap = storeNameResolver.resolveAllWithFallback(ids);

        // 3) 엑셀 생성
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("재고로그_" + materialId);
            String[] cols = {"로그ID", "일시", "유형", "수량", "재고후", "단가", "메모", "가맹점명"};

            // 헤더
            Row h = sheet.createRow(0);
            CellStyle hs = wb.createCellStyle();
            Font f = wb.createFont();
            f.setBold(true);
            hs.setFont(f);
            for (int i = 0; i < cols.length; i++) {
                Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(hs);
            }

            // 데이터
            int r = 1;
            for (InventoryLogView v : page) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(v.getLogId());
                row.createCell(1).setCellValue(v.getDate() == null ? "" : v.getDate().toString());
                row.createCell(2).setCellValue(v.getType() == null ? "" : v.getType());
                row.createCell(3).setCellValue(v.getQuantity() == null ? 0d : v.getQuantity().doubleValue());
                row.createCell(4).setCellValue(v.getStockAfter() == null ? 0d : v.getStockAfter().doubleValue());
                row.createCell(5).setCellValue(v.getUnitPrice() == null ? 0d : v.getUnitPrice().doubleValue());
                row.createCell(6).setCellValue(v.getMemo() == null ? "" : v.getMemo());
                String storeName = v.getStoreId() == null ? "" : nameMap.getOrDefault(v.getStoreId(), "");
                row.createCell(7).setCellValue(storeName);
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new IllegalStateException("재고 로그 엑셀 생성 실패", e);
        }
    }

    /**
     * 본사 재고 배치(LOT) 목록을 XLSX로 생성한다.
     *
     * <p>해당 재료의 전체 배치(잔량 0 포함)를 화면과 동일한 정렬로 덤프한다.</p>
     *
     * @param materialId 재료 ID
     * @return XLSX 바이트 배열
     * @throws IOException 워크북 쓰기/닫기 중 I/O 오류
     */
    @Transactional(readOnly = true)
    public byte[] downloadBatchExcel(final Long materialId) throws IOException {
        // 화면과 동일한 정렬
        List<InventoryBatch> batches =
                inventoryBatchRepository.findAllByMaterial_IdOrderByReceivedDateDesc(materialId);

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Batches");
            int rowIdx = 0;

            // 헤더
            Row header = sheet.createRow(rowIdx++);
            int hc = 0;
            header.createCell(hc++).setCellValue("배치ID");
            header.createCell(hc++).setCellValue("LOT 번호");
            header.createCell(hc++).setCellValue("입고일");
            header.createCell(hc++).setCellValue("유통기한");
            header.createCell(hc++).setCellValue("입고수량");
            header.createCell(hc++).setCellValue("잔량");
            header.createCell(hc++).setCellValue("입고단가");

            // 데이터
            DateTimeFormatter dtfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (InventoryBatch b : batches) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                row.createCell(c++).setCellValue(b.getId());
                row.createCell(c++).setCellValue(b.getLotNo() != null ? b.getLotNo() : "");
                row.createCell(c++).setCellValue(
                        b.getReceivedDate() != null ? b.getReceivedDate().format(dtfDateTime) : ""
                );
                row.createCell(c++).setCellValue(
                        b.getExpirationDate() != null ? b.getExpirationDate().format(dtfDate) : ""
                );
                row.createCell(c++).setCellValue(
                        b.getReceivedQuantity() != null ? b.getReceivedQuantity().doubleValue() : 0d
                );
                row.createCell(c++).setCellValue(
                        b.getQuantity() != null ? b.getQuantity().doubleValue() : 0d
                );
                row.createCell(c++).setCellValue(
                        b.getUnitPrice() != null ? b.getUnitPrice().doubleValue() : 0d
                );
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * 특정 LOT(배치)의 출고 이력을 XLSX로 생성한다.
     *
     * <p>
     * 상단에 LOT 요약(LOT 번호/입고일/입고수량/현재잔량/유통기한/입고단가)을 배치하고,
     * 하단에 출고 이력 테이블(출고일시/가맹점/출고수량/메모)을 출력한다.
     * </p>
     *
     * @param batchId 배치 ID
     * @return XLSX 바이트 배열
     * @throws IOException 워크북 쓰기/닫기 중 I/O 오류
     */
    @Transactional(readOnly = true)
    public byte[] downloadLotOutHistoryExcel(final Long batchId) throws IOException {
        // LOT 상세(상단 요약 + 파일명 등)
        InventoryLotDetailDTO lot = inventoryBatchService.getLotDetail(batchId);

        // 출고 이력 전체 조회(필요 시 size 조정)
        Page<InventoryOutLotHistoryRowDTO> page =
                inventoryLotService.getOutLotHistory(batchId, PageRequest.of(0, 1000));
        List<InventoryOutLotHistoryRowDTO> rows = page.getContent();

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("LotOutHistory");
            int rowIdx = 0;

            DateTimeFormatter dtfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            DateTimeFormatter dtfDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            /* ================= LOT 요약 섹션 ================= */
            if (lot != null) {
                Row lotRow0 = sheet.createRow(rowIdx++);
                lotRow0.createCell(0).setCellValue("LOT 번호");
                lotRow0.createCell(1).setCellValue(lot.getLotNo() != null ? lot.getLotNo() : "");

                Row lotRow1 = sheet.createRow(rowIdx++);
                lotRow1.createCell(0).setCellValue("입고일");
                lotRow1.createCell(1).setCellValue(
                        lot.getReceivedDate() != null ? lot.getReceivedDate().format(dtfDateTime) : ""
                );

                Row lotRow2 = sheet.createRow(rowIdx++);
                lotRow2.createCell(0).setCellValue("입고수량");
                lotRow2.createCell(1).setCellValue(
                        lot.getReceivedQuantity() != null ? lot.getReceivedQuantity().doubleValue() : 0d
                );

                Row lotRow3 = sheet.createRow(rowIdx++);
                lotRow3.createCell(0).setCellValue("현재잔량");
                lotRow3.createCell(1).setCellValue(
                        lot.getRemainingQuantity() != null ? lot.getRemainingQuantity().doubleValue() : 0d
                );

                Row lotRow4 = sheet.createRow(rowIdx++);
                lotRow4.createCell(0).setCellValue("유통기한");
                lotRow4.createCell(1).setCellValue(
                        lot.getExpirationDate() != null ? lot.getExpirationDate().format(dtfDate) : ""
                );

                Row lotRow5 = sheet.createRow(rowIdx++);
                lotRow5.createCell(0).setCellValue("입고단가");
                lotRow5.createCell(1).setCellValue(
                        lot.getUnitPrice() != null ? lot.getUnitPrice().doubleValue() : 0d
                );

                // 요약과 이력 사이 한 줄 공백
                rowIdx++;
            }

            /* ================= 출고 이력 테이블 ================= */
            Row header = sheet.createRow(rowIdx++);
            int hc = 0;
            header.createCell(hc++).setCellValue("출고일시");
            header.createCell(hc++).setCellValue("가맹점");
            header.createCell(hc++).setCellValue("출고 수량");
            header.createCell(hc++).setCellValue("메모");

            for (InventoryOutLotHistoryRowDTO r : rows) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                String outDateStr = (r.getOutDate() != null) ? r.getOutDate().format(dtfDateTime) : "";
                row.createCell(c++).setCellValue(outDateStr);
                row.createCell(c++).setCellValue(r.getStoreName() != null ? r.getStoreName() : "");

                BigDecimal qty = r.getQty();
                row.createCell(c++).setCellValue(qty != null ? qty.doubleValue() : 0d);

                row.createCell(c++).setCellValue(r.getMemo() != null ? r.getMemo() : "");
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    /**
     * 인벤토리 현재고를 지정 값으로 동기화한다(배치 합계 반영 용도).
     *
     * @param materialId 재료 ID
     * @param newQty     동기화할 현재고 값
     */
    @Transactional
    public void syncInventoryQuantity(final Long materialId, final BigDecimal newQty) {
        inventoryRepository.findByMaterialId(materialId)
                .ifPresent(inv -> {
                    inv.setQuantity(newQty);
                    inv.updateStatusNow(); // 상태 재계산 + 갱신일시 반영
                });
    }
}
