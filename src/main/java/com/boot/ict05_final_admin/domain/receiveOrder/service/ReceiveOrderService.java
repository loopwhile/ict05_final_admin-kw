package com.boot.ict05_final_admin.domain.receiveOrder.service;

import com.boot.ict05_final_admin.domain.inventory.service.InventoryOutService;
import com.boot.ict05_final_admin.domain.receiveOrder.dto.*;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrder;
import com.boot.ict05_final_admin.domain.receiveOrder.entity.ReceiveOrderStatus;
import com.boot.ict05_final_admin.domain.receiveOrder.repository.ReceiveOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 수주(Receive Order) 서비스 클래스
 *
 * <p>본 클래스는 본사에서 관리하는 수주(가맹점 발주) 관련 주요 로직을 처리한다.
 * 다음 기능들을 포함한다:</p>
 *
 * <ul>
 *     <li>수주 목록 조회 (검색 및 페이징)</li>
 *     <li>수주 상세 내역 조회 (하위 품목 포함)</li>
 *     <li>배송 상태 단계별 변경</li>
 *     <li>상단 대시보드 요약 정보 조회</li>
 *     <li>수주 목록 엑셀 다운로드</li>
 * </ul>
 *
 * <p>DB 접근은 {@link ReceiveOrderRepository}를 통해 수행된다.</p>
 *
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class ReceiveOrderService {

    private final ReceiveOrderRepository receiveOrderRepository;

    private final InventoryOutService inventoryOutService;

    /**
     * 수주 목록을 페이지 단위로 조회한다.
     *
     * @param receiveOrderSearchDTO  (선택, null 가능)
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이징 처리된 수주 리스트 DTO
     */
    public Page<ReceiveOrderListDTO> selectAllOfficeReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) {
        return receiveOrderRepository.listReceive(receiveOrderSearchDTO, pageable);
    }

    /**
     * 특정 수주의 상세 정보를 조회한다.
     *
     * <p>상위 수주({@link ReceiveOrder}) 정보와 함께
     * 하위 품목 목록({@link ReceiveOrderItemDTO})을 함께 반환한다.</p>
     *
     * @param id 수주 ID
     * @return 수주 상세 DTO (품목 리스트 포함)
     * @throws NoSuchElementException 수주가 존재하지 않을 경우
     */
    public ReceiveOrderDetailDTO getReceiveOrderDetail(Long id) {
        ReceiveOrderDetailDTO dto = receiveOrderRepository.findDetailById(id)
                .orElseThrow(() -> new NoSuchElementException("수주 내역이 존재하지 않습니다. id=" + id));

        List<ReceiveOrderItemDTO> items = receiveOrderRepository.findItemsByOrderId(id);
        dto.setItems(items);  // setter로 주입

        return dto;
    }

    /**
     * 지정된 수주의 상태를 다음 단계로 전환한다.
     *
     * <p>상태 전환 순서:
     *  * RECEIVED → SHIPPING → DELIVERED<br>
     *  * 또는 RECEIVED → CANCELED</p>
     * <p>
     *     action 이 "SHIP" 이고 상태가 RECEIVED → SHIPPING 으로 변경될 때,
     *     해당 수주에 대해 본사 재고에서 가맹점으로 출고를 생성한다.
     *     출고 생성은 {@link InventoryOutService#createOutByReceiveOrder(ReceiveOrderDetailDTO)} 를 사용한다.</p>
     *
     * @param id 수주 ID
     * @param action "SHIP" 또는 "CANCEL"
     * @throws IllegalArgumentException 수주가 존재하지 않을 경우
     * @throws IllegalStateException 이미 완료된 주문일 경우
     */
    public void updateStatus(Long id, String action) {
        ReceiveOrder order = receiveOrderRepository.findOrderById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 없습니다. id=" + id));

        ReceiveOrderStatus curr = order.getStatus();
        ReceiveOrderStatus next;
        switch (action.toUpperCase()) {
            case "SHIP":
                if (curr != ReceiveOrderStatus.RECEIVED) throw new IllegalStateException("invalid");
                next = ReceiveOrderStatus.SHIPPING;
                break;
            case "CANCEL":
                if (curr != ReceiveOrderStatus.RECEIVED) throw new IllegalStateException("invalid");
                next = ReceiveOrderStatus.CANCELED;
                break;
            default: throw new IllegalArgumentException("unknown action");
        }

        int updated = receiveOrderRepository.updateStatusIfCurrent(id, curr, next);
        if (updated == 0) {
            throw new IllegalStateException("상태 업데이트 실패: id=" + id);
        }

        // RECEIVED → SHIPPING 으로 전환된 경우에만 출고 생성
        if (next == ReceiveOrderStatus.SHIPPING) {
            // 수주 상세 + 품목 DTO를 조회
            ReceiveOrderDetailDTO orderDetail = getReceiveOrderDetail(id);

            try {
                // 본사 → 가맹점 출고 생성 (FIFO + 현재고 반영은 InventoryOutService 가 담당)
                inventoryOutService.createOutByReceiveOrder(orderDetail);
            }catch (IllegalStateException e) {
                log.error("[updateStatus] 출고 생성 중 예외 발생 id={} msg={}", id, e.getMessage(), e);
                throw e; // 그대로 던져서 409 유지
            }
        }
        // 가맹점으로 동기화 콜 (이중 시스템일 때만)
        // orderSyncService.syncFromHQ(order.getOrderCode(), next);
    }

    /**
     * 가맹점 시스템에서 전달한 상태를 본사 수주 상태에 반영한다.
     *
     * <p>
     * 주로 가맹점 측에서 배송 완료/취소 등 이벤트가 발생했을 때,
     * 주문코드 기준으로 본사 수주 상태를 동기화하는 용도로 사용한다.
     * </p>
     *
     * @param orderCode 가맹점/본사 공통으로 사용하는 주문 번호
     * @param status    변경할 상태 문자열 (RECEIVED, SHIPPING, DELIVERED, CANCELED 등)
     * @throws IllegalArgumentException 해당 주문 코드가 없을 경우
     */
    public void applyStatusFromStore(String orderCode, String status) {
        ReceiveOrderStatus next = ReceiveOrderStatus.valueOf(status.toUpperCase());
        int updated = receiveOrderRepository.updateStatusByOrderCode(orderCode, next);
        if (updated == 0) throw new IllegalArgumentException("해당 주문코드 없음: " + orderCode);
    }

    /**
     * 수주 현황 요약 데이터를 조회한다.
     *
     * <p>상단 카드에 표시되는 주요 통계 정보를 반환한다.</p>
     *
     * @return 총 주문 수, 총 주문액, 배송 중 수량, 긴급 주문 수 포함 요약 DTO
     */
    public ReceiveOrderSummaryDTO getSummary() {
        return receiveOrderRepository.getSummary();
    }

    /**
     * 수주 목록을 Excel 파일로 생성하여 다운로드한다.
     *
     * <p>검색 조건 및 페이징 정보에 따라 데이터를 조회하고,
     * Apache POI를 이용해 Excel 워크북을 생성한다.</p>
     *
     * @param receiveOrderSearchDTO 검색 조건 DTO
     * @param pageable 페이지 정보
     * @return 엑셀 파일 데이터 (byte[])
     * @throws IOException Excel 파일 생성 중 오류 발생 시
     */
    public byte[] downloadExcel(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("수주 목록");

        // 날짜 포맷 스타일 생성
        CellStyle dateCellStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("yyyy-MM-dd")
        );

        // 금액 포맷 (천 단위 콤마)
        CellStyle moneyCellStyle = workbook.createCellStyle();
        moneyCellStyle.setDataFormat(
                createHelper.createDataFormat().getFormat("#,##0")
        );

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("가맹점명");
        header.createCell(2).setCellValue("주문번호");
        header.createCell(3).setCellValue("지역");
        header.createCell(4).setCellValue("상태");
        header.createCell(5).setCellValue("우선순위");
        header.createCell(6).setCellValue("주문액");
        header.createCell(7).setCellValue("품목수");
        header.createCell(8).setCellValue("배송완료일");

        long count = receiveOrderRepository.countReceive(receiveOrderSearchDTO);
        PageRequest pageRequest = PageRequest.of(0, (int) count, Sort.by("id").descending());
        Page<ReceiveOrderListDTO> list = receiveOrderRepository.listReceive(receiveOrderSearchDTO, pageRequest);

        int i = 1;
        for (ReceiveOrderListDTO ro : list) {
            Row sheet1_row = sheet.createRow(i);

            sheet1_row.createCell(0).setCellValue(ro.getId());
            sheet1_row.createCell(1).setCellValue(ro.getStoreName());
            sheet1_row.createCell(2).setCellValue(ro.getOrderCode());
            sheet1_row.createCell(3).setCellValue(ro.getStoreLocation());
            sheet1_row.createCell(4).setCellValue(String.valueOf(ro.getStatus()));
            sheet1_row.createCell(5).setCellValue(String.valueOf(ro.getPriority()));
            Cell priceCell = sheet1_row.createCell(6);
            priceCell.setCellValue(ro.getTotalPrice() != null ? ro.getTotalPrice().doubleValue() : 0.0);
            priceCell.setCellStyle(moneyCellStyle);
            sheet1_row.createCell(7).setCellValue(ro.getTotalCount());

            if (ro.getActualDeliveryDate() != null) {
                Cell dateCell = sheet1_row.createCell(8);
                Date excelDate = Date.from(ro.getActualDeliveryDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
                dateCell.setCellValue(excelDate);
                dateCell.setCellStyle(dateCellStyle);
            } else {
                sheet1_row.createCell(8).setCellValue("");
            }
            i++;
        }

        // 모든 열을 내용 길이에 맞게 자동 조정
        for (int col = 0; col <= 8; col++) {
            int maxLength = 0;

            // 헤더 포함 전체 행 탐색
            for (int rowIdx = 0; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
                Row row = sheet.getRow(rowIdx);
                if (row != null) {
                    Cell cell = row.getCell(col);
                    if (cell != null) {
                        int length = cell.toString().getBytes(StandardCharsets.UTF_8).length;
                        if (length > maxLength) maxLength = length;
                    }
                }
            }

            // 글자 수 × 256 단위로 변환 (엑셀 단위), 여유 폭 +2글자
            sheet.setColumnWidth(col, (maxLength + 2) * 256);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    /**
     * 수주 상세 주문서를 Excel로 생성한다.
     *
     * <p>주문 기본 정보(주문번호, 주문일, 배송예정일, 상태, 우선순위),
     * 가맹점 정보(가맹점명, 매장코드, 지역, 총 주문액, 주문 품목수),
     * 그리고 주문 상품 목록(상품명, 카테고리, 수량, 단가, 총액, 재고상태)을
     * 모두 포함한 상세 주문서를 생성한다.</p>
     *
     * @param orderId 수주 ID
     * @return Excel 파일 바이트 배열
     * @throws IOException Excel 생성 중 오류 시
     */
    @Transactional(readOnly = true)
    public byte[] downloadDetailExcel(Long orderId) throws IOException {

        ReceiveOrderDetailDTO order = getReceiveOrderDetail(orderId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("주문상세");
        CreationHelper helper = workbook.getCreationHelper();

        // ====== 스타일 정의 ======
        CellStyle leftAlign = workbook.createCellStyle();
        leftAlign.setAlignment(HorizontalAlignment.LEFT);

        // 날짜 스타일 (왼쪽 정렬)
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(helper.createDataFormat().getFormat("yyyy-MM-dd"));
        dateStyle.setAlignment(HorizontalAlignment.LEFT);

        // 금액(천단위 콤마, 왼쪽 정렬)
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.setDataFormat(helper.createDataFormat().getFormat("#,##0"));
        moneyStyle.setAlignment(HorizontalAlignment.LEFT);

        int rowIdx = 0;

        // ====== 제목 ======
        Row title = sheet.createRow(rowIdx++);
        Cell titleCell = title.createCell(0);
        titleCell.setCellValue("수주 상세 주문서");
        titleCell.setCellStyle(leftAlign);
        rowIdx++;

        // ====== 주문 정보 ======
        Row header1 = sheet.createRow(rowIdx++);
        header1.createCell(0).setCellValue("주문 정보");

        Row orderInfo1 = sheet.createRow(rowIdx++);
        orderInfo1.createCell(0).setCellValue("주문번호");
        orderInfo1.createCell(1).setCellValue(order.getOrderCode());
        orderInfo1.createCell(2).setCellValue("주문일");

        Cell orderDateCell = orderInfo1.createCell(3);
        if (order.getOrderDate() != null) {
            orderDateCell.setCellValue(Date.from(order.getOrderDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            orderDateCell.setCellStyle(dateStyle);
        }

        Row orderInfo2 = sheet.createRow(rowIdx++);
        orderInfo2.createCell(0).setCellValue("배송완료일");
        Cell actualDeliveryDateCell = orderInfo2.createCell(1);
        if (order.getActualDeliveryDate() != null) {
            actualDeliveryDateCell.setCellValue(Date.from(order.getActualDeliveryDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            actualDeliveryDateCell.setCellStyle(dateStyle);
        }
        orderInfo2.createCell(2).setCellValue("상태");
        orderInfo2.createCell(3).setCellValue(order.getStatusDescription());

        Row orderInfo3 = sheet.createRow(rowIdx++);
        orderInfo3.createCell(0).setCellValue("우선순위");
        orderInfo3.createCell(1).setCellValue(order.getPriorityDescription());

        rowIdx++;

        // ====== 가맹점 정보 ======
        Row header2 = sheet.createRow(rowIdx++);
        header2.createCell(0).setCellValue("가맹점 정보");

        Row storeInfo1 = sheet.createRow(rowIdx++);
        storeInfo1.createCell(0).setCellValue("가맹점명");
        storeInfo1.createCell(1).setCellValue(order.getStoreName());
        storeInfo1.createCell(2).setCellValue("매장코드");
        Cell storeIdCell = storeInfo1.createCell(3);
        storeIdCell.setCellValue(order.getStoreId());
        storeIdCell.setCellStyle(leftAlign);

        Row storeInfo2 = sheet.createRow(rowIdx++);
        storeInfo2.createCell(0).setCellValue("지역");
        storeInfo2.createCell(1).setCellValue(order.getStoreLocation());
        storeInfo2.createCell(2).setCellValue("총 주문액");
        Cell totalPriceCell = storeInfo2.createCell(3);
        totalPriceCell.setCellValue(order.getTotalPrice() != null ? order.getTotalPrice().doubleValue() : 0.0);
        totalPriceCell.setCellStyle(moneyStyle);

        Row storeInfo3 = sheet.createRow(rowIdx++);
        storeInfo3.createCell(0).setCellValue("주문 품목");
        Cell totalCountCell = storeInfo3.createCell(1);
        totalCountCell.setCellValue(order.getTotalCount());
        totalCountCell.setCellStyle(leftAlign);

        rowIdx += 2;

        // ====== 주문 상품 ======
        Row header3 = sheet.createRow(rowIdx++);
        header3.createCell(0).setCellValue("주문 상품");

        Row tableHeader = sheet.createRow(rowIdx++);
        String[] headers = {"상품명", "카테고리", "수량", "단가", "총액", "재고상태"};
        for (int i = 0; i < headers.length; i++) {
            Cell headerCell = tableHeader.createCell(i);
            headerCell.setCellValue(headers[i]);
            headerCell.setCellStyle(leftAlign);
        }

        for (ReceiveOrderItemDTO item : order.getItems()) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(item.getName());
            row.createCell(1).setCellValue(item.getMaterialCategoryDescription());

            Cell countCell = row.createCell(2);
            countCell.setCellValue(item.getDetailCount());
            countCell.setCellStyle(leftAlign);

            Cell unitPrice = row.createCell(3);
            unitPrice.setCellValue(item.getDetailUnitPrice() != null ? item.getDetailUnitPrice().doubleValue() : 0.0);
            unitPrice.setCellStyle(moneyStyle);

            Cell totalPrice = row.createCell(4);
            totalPrice.setCellValue(item.getDetailTotalPrice() != null ? item.getDetailTotalPrice().doubleValue() : 0.0);
            totalPrice.setCellStyle(moneyStyle);

            Cell statusCell = row.createCell(5);
            statusCell.setCellValue(item.getInventoryStatusDescription());
            statusCell.setCellStyle(leftAlign);
        }

        rowIdx += 2;

        // ====== 특이사항 ======
        Row remarkHeader = sheet.createRow(rowIdx++);
        remarkHeader.createCell(0).setCellValue("특이사항");
        Row remarkRow = sheet.createRow(rowIdx++);
        remarkRow.createCell(0).setCellValue(order.getRemark() != null ? order.getRemark() : "-");

        // ====== 자동열 폭 조정 ======
        for (int col = 0; col <= 6; col++) {
            sheet.autoSizeColumn(col); // 실제 데이터 기준 폭 조정
            int width = sheet.getColumnWidth(col);
            sheet.setColumnWidth(col, width + 1500); // 여유 폭 확보 (붙는 현상 방지)
        }

        // ====== 엑셀 출력 ======
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return out.toByteArray();
    }

}
