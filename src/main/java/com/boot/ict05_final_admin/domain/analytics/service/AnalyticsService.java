package com.boot.ict05_final_admin.domain.analytics.service;

import com.boot.ict05_final_admin.config.PythonPdfClient;
import com.boot.ict05_final_admin.domain.analytics.dto.*;
import com.boot.ict05_final_admin.domain.analytics.repository.AnalyticsRepository;
import com.boot.ict05_final_admin.domain.analytics.util.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.time.format.DateTimeFormatter;

/**
 * 통계(Analytics) 도메인의 조회·내보내기(엑셀/PDF) 비즈니스 로직 서비스.
 *
 * <p>
 * KPI, 주문, 재료, 시간·요일 분석에 대한 카드/테이블 데이터 조회와
 * 엑셀(XLSX) 및 PDF 생성 바이트를 제공한다. 조회성 메서드는 기본적으로
 * {@code @Transactional(readOnly = true)}로 동작하며, 대량 내보내기는
 * 전체 건수를 선조회한 후 단일 페이징으로 전체를 조회하는 패턴을 따른다.
 * </p>
 *
 * <h3>성능 원칙</h3>
 * <ul>
 *   <li>조회 메서드: 읽기 전용 트랜잭션 + Repository의 DTO 프로젝션 사용</li>
 *   <li>엑셀: {@link SXSSFWorkbook} 스트리밍 사용으로 메모리 사용 최소화</li>
 *   <li>PDF: Python(ReportLab) 마이크로서비스 연동, 안전한 직렬화를 위한 Map 변환</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final PythonPdfClient pythonPdfClient;

    /**
     * KPI 카드(요약) 데이터를 조회한다.
     *
     * @return KPI 카드 요약 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public KpiCardsDto selectKpiCards() {
        return analyticsRepository.findKpiSummary();
    }

    /**
     * KPI 테이블 데이터를 페이지 단위로 조회한다.
     *
     * @param cond     조회 조건
     * @param pageable 페이지 정보
     * @return KPI 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<KpiRowDto> selectKpis(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findKpi(cond, pageable);
    }

    /**
     * 주문 카드(요약) 데이터를 조회한다.
     *
     * @return 주문 카드 요약 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public OrdersCardsDto selectOrdersCards() {
        return analyticsRepository.findOrdersSummary();
    }

    /**
     * 주문 테이블 데이터를 페이지 단위로 조회한다.
     *
     * @param cond     조회 조건
     * @param pageable 페이지 정보
     * @return 주문 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<OrdersRowDto> selectOrders(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findOrders(cond, pageable);
    }

    /**
     * 재료 카드(요약) 데이터를 조회한다.
     *
     * @return 재료 카드 요약 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public MaterialsCardsDto selectMaterialsCards() {
        return analyticsRepository.findMaterialsSummary();
    }

    /**
     * 재료 테이블 데이터를 페이지 단위로 조회한다.
     *
     * @param cond     조회 조건
     * @param pageable 페이지 정보
     * @return 재료 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<MaterialsRowDto> selectMaterials(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findMaterials(cond, pageable);
    }

    /**
     * 시간·요일 분석 카드(요약) 데이터를 조회한다.
     *
     * @return 시간·요일 카드 요약 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public TimeChartCardDto selectTimeChartCards() {
        return analyticsRepository.findTimeChartSummary();
    }

    /**
     * 시간·요일 분석 차트 데이터를 조회한다.
     *
     * @param cond 조회 조건
     * @return 시간·요일 차트 DTO
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public TimeChartRowDto selectTimeChart(AnalyticsSearchDto cond) {
        return analyticsRepository.findTimeChart(cond);
    }

    /**
     * 시간·요일 분석 테이블 데이터를 페이지 단위로 조회한다.
     *
     * @param cond     조회 조건
     * @param pageable 페이지 정보
     * @return 시간·요일 행 페이지
     */
    @LogExecutionTime
    @Transactional(readOnly = true)
    public Page<TimeRowDto> selectTimeRows(AnalyticsSearchDto cond, Pageable pageable) {
        return analyticsRepository.findTimeRows(cond, pageable);
    }

    /**
     * KPI 테이블을 엑셀(XLSX)로 생성하여 바이트 배열로 반환한다.
     *
     * <p>
     * 전체 건수를 선조회한 뒤 한 페이지로 모두 가져와 워크북을 구성한다.
     * 통화/정수/소수/퍼센트 셀 스타일을 적용하여 가독성을 높인다.
     * </p>
     *
     * @param cond     조회 조건
     * @param pageable 페이지 정보(엑셀 내에서는 전체 다운로드를 위해 무시됨)
     * @return XLSX 바이트 배열(없으면 길이 0)
     * @throws RuntimeException 엑셀 생성 실패 시
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcelKpi(AnalyticsSearchDto cond, Pageable pageable) {
        long total = analyticsRepository.countKpi(cond);
        if (total == 0) {
            return new byte[0];
        }

        Pageable fullPage = PageRequest.of(0, (int) total);
        Page<KpiRowDto> page = analyticsRepository.findKpi(cond, fullPage);
        List<KpiRowDto> rows = page.getContent();

        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("KPI");
            DataFormat df = wb.createDataFormat();

            CellStyle head = createHeaderStyle(wb);
            CellStyle text = createBodyStyle(wb);
            CellStyle money = createMoneyStyle(wb, text, df);
            CellStyle intStyle = createIntegerStyle(wb, text, df);
            CellStyle dec1 = createDecimalStyle(wb, text, df, "0.0");
            CellStyle pct1 = createPercentStyle(wb, text, df, "0.0%");

            String[] headers = {"Date","Store","Sales","Transaction","UPT","ADS","AUR","Comp.(MoM)","Comp.(YoY)"};
            Row hr = sheet.createRow(0);
            for (int c=0; c<headers.length; c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(head);
            }

            int r = 1;
            for (KpiRowDto dto : rows) {
                Row row = sheet.createRow(r++);
                setText(row,0, dto.getDate(), text);
                setText(row,1, dto.getStoreName(), text);
                setNum (row,2, dto.getSales(), money);
                setNum (row,3, dto.getTransaction(), intStyle);
                setNum (row,4, dto.getUpt(), dec1);
                setNum (row,5, dto.getAds(), money);
                setNum (row,6, dto.getAur(), money);
                setPct (row,7, dto.getCompMoM(), pct1);
                setPct (row,8, dto.getCompYoY(), pct1);
            }

            sheet.setColumnWidth(0, 12 * 256);
            sheet.setColumnWidth(1, 15 * 256);
            for (int c=2; c<9; c++) sheet.setColumnWidth(c, 12 * 256);

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    /**
     * 주문 테이블을 엑셀(XLSX)로 생성하여 바이트 배열로 반환한다.
     *
     * <p>
     * 조회 모드가 일(DAY)인 경우 주문 상세 컬럼을 포함하여 헤더/본문을 구성한다.
     * 금액/개수에 대한 서식을 적용한다.
     * </p>
     *
     * @param cond     조회 조건
     * @param pageable 페이지 정보(엑셀 내에서는 전체 다운로드를 위해 무시됨)
     * @return XLSX 바이트 배열(없으면 길이 0)
     * @throws RuntimeException 엑셀 생성 실패 시
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcelOrders(AnalyticsSearchDto cond, Pageable pageable) {
        long total = analyticsRepository.countOrders(cond);
        if (total == 0) {
            return new byte[0];
        }

        Pageable fullPage = PageRequest.of(0, (int) total);
        Page<OrdersRowDto> page = analyticsRepository.findOrders(cond, fullPage);
        List<OrdersRowDto> rows = page.getContent();

        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Orders");
            DataFormat df = wb.createDataFormat();

            CellStyle head = createHeaderStyle(wb);
            CellStyle text = createBodyStyle(wb);
            CellStyle money = createMoneyStyle(wb, text, df);
            CellStyle intStyle = createIntegerStyle(wb, text, df);

            boolean isDailyView = cond.getViewBy() == ViewBy.DAY;

            List<String> headerList = new ArrayList<>(Arrays.asList("Date", "Store", "MenuCount", "MenuSales", "OrderCount", "OrderSales"));
            if (isDailyView) {
                headerList.add(1, "OrderDate");
                headerList.add(3, "OrderId");
                headerList.add(4, "Category");
                headerList.add(5, "Menu");
                headerList.add("OrderType");
            }

            Row hr = sheet.createRow(0);
            for (int c = 0; c < headerList.size(); c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headerList.get(c));
                cell.setCellStyle(head);
            }

            int r = 1;
            for (OrdersRowDto dto : rows) {
                Row row = sheet.createRow(r++);
                int col = 0;
                setText(row, col++, dto.getDate(), text);
                if (isDailyView) {
                    setText(row, col++, dto.getOrderDate(), text);
                }
                setText(row, col++, dto.getStoreName(), text);
                if (isDailyView) {
                    setText(row, col++, dto.getOrderId() != null ? dto.getOrderId().toString() : "", text);
                    setText(row, col++, dto.getCategory(), text);
                    setText(row, col++, dto.getMenu(), text);
                }
                setNum(row, col++, dto.getMenuCount(), intStyle);
                setNum(row, col++, dto.getMenuSales(), money);
                setNum(row, col++, dto.getOrderCount(), intStyle);
                setNum(row, col++, dto.getOrderSales(), money);
                if (isDailyView) {
                    setText(row, col++, dto.getOrderType(), text);
                }
            }

            for (int c = 0; c < headerList.size(); c++) {
                sheet.setColumnWidth(c, 15 * 256);
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    /**
     * 시간·요일 분석 테이블을 엑셀(XLSX)로 생성하여 바이트 배열로 반환한다.
     *
     * <p>
     * 전체 건수를 선조회하여 한 번에 모두 읽어들인 뒤, 일/월 모드에 따라 헤더 구성을 달리한다.
     * 금액/문자열 서식을 적용하고, UI 테이블 컬럼 구성과 동일한 순서로 내보낸다.
     * </p>
     *
     * @param cond     조회 조건
     * @param pageable 페이지 정보(엑셀 내에서는 전체 다운로드를 위해 무시됨)
     * @return XLSX 바이트 배열(없으면 길이 0)
     * @throws RuntimeException 엑셀 생성 실패 시
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcelTime(AnalyticsSearchDto cond, Pageable pageable) {
        long total = analyticsRepository.countTime(cond);
        if (total == 0) return new byte[0];

        Pageable fullPage = PageRequest.of(0, (int) total);
        Page<TimeRowDto> page = analyticsRepository.findTimeRows(cond, fullPage);
        List<TimeRowDto> rows = page.getContent();

        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Time");
            DataFormat df = wb.createDataFormat();

            CellStyle head    = createHeaderStyle(wb);
            CellStyle text    = createBodyStyle(wb);
            CellStyle money   = createMoneyStyle(wb, text, df);
            CellStyle intStyle= createIntegerStyle(wb, text, df);

            boolean isDailyView = (cond.getViewBy() == ViewBy.DAY);

            List<String> headerList;
            if (isDailyView) {
                headerList = Arrays.asList(
                        "Store", "시간대", "요일", "주문ID", "주문금액",
                        "카테고리", "메뉴", "OrderType", "OrderDate"
                );
            } else {
                headerList = Arrays.asList(
                        "Date", "Store", "시간대", "요일", "주문금액", "OrderType"
                );
            }

            Row hr = sheet.createRow(0);
            for (int c = 0; c < headerList.size(); c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headerList.get(c));
                cell.setCellStyle(head);
            }

            int r = 1;
            for (TimeRowDto dto : rows) {
                Row row = sheet.createRow(r++);
                int col = 0;

                if (isDailyView) {
                    setText(row, col++, dto.getStoreName(),  text);
                    setText(row, col++, dto.getHourSlot(),   text);
                    setText(row, col++, dto.getDayOfWeek(),  text);
                    setText(row, col++, dto.getOrderId() != null ? dto.getOrderId().toString() : "", text);
                    setNum (row, col++, dto.getOrderAmount(), money);
                    setText(row, col++, dto.getCategory(),   text);
                    setText(row, col++, dto.getMenu(),       text);
                    setText(row, col++, dto.getOrderType(),  text);
                    setText(row, col++, dto.getOrderDate(),  text);
                } else {
                    setText(row, col++, dto.getDate(),       text);
                    setText(row, col++, dto.getStoreName(),  text);
                    setText(row, col++, dto.getHourSlot(),   text);
                    setText(row, col++, dto.getDayOfWeek(),  text);
                    setNum (row, col++, dto.getOrderAmount(), money);
                    setText(row, col++, dto.getOrderType(),  text);
                }
            }

            for (int c = 0; c < headerList.size(); c++) {
                int w = switch (headerList.get(c)) {
                    case "Store"     -> 18;
                    case "시간대"       -> 14;
                    case "요일"        -> 8;
                    case "주문ID"       -> 12;
                    case "주문금액"      -> 14;
                    case "카테고리"      -> 16;
                    case "메뉴"        -> 20;
                    case "OrderType" -> 12;
                    case "OrderDate" -> 20;
                    case "Date"      -> 12;
                    default -> 15;
                };
                sheet.setColumnWidth(c, w * 256);
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    /**
     * 재료 테이블을 엑셀(XLSX)로 생성하여 바이트 배열로 반환한다.
     *
     * <p>
     * 전체 건수를 선조회하여 한 번에 모두 읽어들인 뒤, 헤더/본문을 구성한다.
     * 금액/개수/비율에 대한 서식을 적용한다.
     * </p>
     *
     * @param cond     조회 조건
     * @param pageable 페이지 정보(엑셀 내에서는 전체 다운로드를 위해 무시됨)
     * @return XLSX 바이트 배열(없으면 길이 0)
     * @throws RuntimeException 엑셀 생성 실패 시
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcelMaterials(AnalyticsSearchDto cond, Pageable pageable) {
        long total = analyticsRepository.countMaterials(cond);
        if (total == 0) {
            return new byte[0];
        }

        Pageable fullPage = PageRequest.of(0, (int) total);
        Page<MaterialsRowDto> page = analyticsRepository.findMaterials(cond, fullPage);
        List<MaterialsRowDto> rows = page.getContent();

        try (SXSSFWorkbook wb = new SXSSFWorkbook();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Materials");
            DataFormat df = wb.createDataFormat();

            CellStyle head = createHeaderStyle(wb);
            CellStyle text = createBodyStyle(wb);
            CellStyle money = createMoneyStyle(wb, text, df);
            CellStyle intStyle = createIntegerStyle(wb, text, df);
            CellStyle dec2 = createDecimalStyle(wb, text, df, "0.00");
            CellStyle pct2 = createPercentStyle(wb, text, df, "0.00%");

            String[] headers = {"Date", "Store", "Material", "Inv. Qty", "PO ID", "PO Date", "PO Qty", "PO Amount", "Turnover", "Profit", "Margin", "Avg. Usage"};
            Row hr = sheet.createRow(0);
            for (int c = 0; c < headers.length; c++) {
                Cell cell = hr.createCell(c);
                cell.setCellValue(headers[c]);
                cell.setCellStyle(head);
            }

            int r = 1;
            for (MaterialsRowDto dto : rows) {
                Row row = sheet.createRow(r++);
                int col = 0;
                setText(row, col++, dto.getOrderDate(), text);
                setText(row, col++, dto.getStore(), text);
                setText(row, col++, dto.getMaterial(), text);
                setNum(row, col++, dto.getStoreInventoryQty(), intStyle);
                setText(row, col++, dto.getPurchaseOrderId() != null ? dto.getPurchaseOrderId().toString() : "", text);
                setText(row, col++, dto.getPurchaseOrderDate(), text);
                setNum(row, col++, dto.getPurchaseOrderQty(), intStyle);
                setNum(row, col++, dto.getPurchaseOrderAmount(), money);
                setNum(row, col++, dto.getTurnoverRate(), dec2);
                setNum(row, col++, dto.getProfit(), money);
                setPct(row, col++, dto.getMargin(), pct2);
                setNum(row, col++, dto.getAvgUsage(), dec2);
            }

            for (int c = 0; c < headers.length; c++) {
                int w = switch (headers[c]) {
                    case "Date", "PO Date" -> 12;
                    case "Store", "Material" -> 20;
                    case "Inv. Qty", "PO Qty" -> 10;
                    case "PO ID", "Turnover", "Margin", "Avg. Usage" -> 12;
                    case "PO Amount", "Profit" -> 14;
                    default -> 15;
                };
                sheet.setColumnWidth(c, w * 256);
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed for materials", e);
        }
    }

    /**
     * 재료 행 데이터를 PDF로 생성하여 바이트 배열로 반환한다.
     *
     * <p>
     * 총 건수가 {@link #MAX_PDF_ROWS}를 초과하면 상한까지만 전송하며,
     * 조건 정보에 절단 여부를 포함한다.
     * </p>
     *
     * @param cond 조회 조건
     * @return PDF 바이트 배열
     */
    @Transactional(readOnly = true)
    public byte[] downloadPdfMaterials(AnalyticsSearchDto cond) {
        long total = analyticsRepository.countMaterials(cond);

        boolean truncated = false;
        int fetchSize = (int) Math.min(total, MAX_PDF_ROWS);
        if (total > MAX_PDF_ROWS) truncated = true;

        Pageable fullPage = PageRequest.of(0, Math.max(fetchSize, 1));
        List<MaterialsRowDto> rawRows = analyticsRepository.findMaterials(cond, fullPage).getContent();

        if (rawRows.size() > fetchSize && fetchSize > 0) {
            rawRows = rawRows.subList(0, fetchSize);
        }

        List<Map<String, Object>> rows = new ArrayList<>(rawRows.size());
        for (MaterialsRowDto d : rawRows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("orderDate", nz(d.getOrderDate()));
            m.put("store", nz(d.getStore()));
            m.put("material", nz(d.getMaterial()));
            m.put("storeInventoryQty", d.getStoreInventoryQty());
            m.put("purchaseOrderId", d.getPurchaseOrderId());
            m.put("purchaseOrderDate", nz(d.getPurchaseOrderDate()));
            m.put("purchaseOrderQty", d.getPurchaseOrderQty());
            m.put("purchaseOrderAmount", d.getPurchaseOrderAmount());
            m.put("turnoverRate", d.getTurnoverRate());
            m.put("profit", d.getProfit());
            m.put("margin", d.getMargin());
            m.put("avgUsage", d.getAvgUsage());
            rows.add(m);
        }
        if (rows.isEmpty()) {
            rows.add(new LinkedHashMap<>());
        }

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("title", "재료 분석 리포트");
        criteria.put("viewBy", cond.getViewBy() != null ? cond.getViewBy().name() : "DAY");
        criteria.put("startDate", cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "");
        criteria.put("endDate", cond.getEndDate() != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE) : "");
        criteria.put("rowCount", rows.size());
        criteria.put("totalCount", total);
        criteria.put("truncated", truncated);

        Map<String, Object> payload = new HashMap<>();
        payload.put("criteria", criteria);
        payload.put("data", rows);

        byte[] pdf = pythonPdfClient.generateMaterialsReportPdf(payload);
        log.info("Materials PDF ready: total={}, sentRows={}, bytes={}", total, rows.size(), (pdf == null ? 0 : pdf.length));
        return pdf;
    }


    /**
     * KPI 행 데이터를 PDF로 생성하여 바이트 배열로 반환한다.
     *
     * <p>
     * 전체 건수 조회 후 한 페이지로 모두 가져와 Python PDF 서비스에 전달한다.
     * 전달 페이로드는 조건(criteria)와 데이터(data)로 분리하여 직렬화 안정성을 확보한다.
     * </p>
     *
     * @param cond 조회 조건
     * @return PDF 바이트 배열(없으면 길이 0)
     */
    @Transactional(readOnly = true)
    public byte[] downloadPdfKpi(AnalyticsSearchDto cond) {
        long total = analyticsRepository.countKpi(cond);
        if (total == 0) return new byte[0];

        Pageable fullPage = PageRequest.of(0, (int) total);
        List<KpiRowDto> rows = analyticsRepository.findKpi(cond, fullPage).getContent();

        rows = new ArrayList<>(rows);

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("title", "KPI 분석 리포트");
        criteria.put("startDate", cond.getStartDate().format(DateTimeFormatter.ISO_DATE));
        criteria.put("endDate", cond.getEndDate().format(DateTimeFormatter.ISO_DATE));

        Map<String, Object> payload = new HashMap<>();
        payload.put("criteria", criteria);
        payload.put("data", rows);

        return pythonPdfClient.generateKpiReportPdf(payload);
    }

    /**
     * PDF 내보내기 시 전송할 최대 행 수 상한.
     * <p>ReportLab 렌더링 시간 및 용량 제한을 고려한 안전한 기본값.</p>
     */
    private static final int MAX_PDF_ROWS = 5_000;

    /**
     * 주문 행 데이터를 PDF로 생성하여 바이트 배열로 반환한다.
     *
     * <p>
     * 총 건수가 {@link #MAX_PDF_ROWS}를 초과할 경우 PDF 전송 행 수를 상한선으로 절단하고,
     * 기준 정보에 {@code truncated=true}를 포함한다.
     * </p>
     *
     * @param cond 조회 조건
     * @return PDF 바이트 배열
     */
    @Transactional(readOnly = true)
    public byte[] downloadPdfOrders(AnalyticsSearchDto cond) {
        long total = analyticsRepository.countOrders(cond);

        boolean truncated = false;
        int fetchSize = (int) Math.min(total, MAX_PDF_ROWS);
        if (total > MAX_PDF_ROWS) truncated = true;

        Pageable fullPage = PageRequest.of(0, Math.max(fetchSize, 1));
        List<OrdersRowDto> rawRows = analyticsRepository.findOrders(cond, fullPage).getContent();

        if (rawRows.size() > fetchSize && fetchSize > 0) {
            rawRows = rawRows.subList(0, fetchSize);
        }

        List<Map<String, Object>> rows = new ArrayList<>(rawRows.size());
        for (OrdersRowDto d : rawRows) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date",       nz(d.getDate()));
            m.put("orderDate",  nz(d.getOrderDate()));
            m.put("storeName",  nz(d.getStoreName()));
            m.put("category",   nz(d.getCategory()));
            m.put("menu",       nz(d.getMenu()));
            m.put("menuCount",  d.getMenuCount());
            m.put("menuSales",  d.getMenuSales());
            m.put("orderCount", d.getOrderCount());
            m.put("orderSales", d.getOrderSales());
            m.put("orderType",  nz(d.getOrderType()));
            rows.add(m);
        }
        if (rows.isEmpty()) {
            rows.add(new LinkedHashMap<>());
        }

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("title", "주문 분석 리포트");
        criteria.put("viewBy",   cond.getViewBy() != null ? cond.getViewBy().name() : "DAY");
        criteria.put("startDate", cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "");
        criteria.put("endDate",   cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "");
        criteria.put("rowCount",  rows.size());
        criteria.put("totalCount", total);
        criteria.put("truncated", truncated);

        Map<String, Object> payload = new HashMap<>();
        payload.put("criteria", criteria);
        payload.put("data", rows);

        byte[] pdf = pythonPdfClient.generateOrdersReportPdf(payload);
        log.info("Orders PDF ready: total={}, sentRows={}, bytes={}", total, rows.size(), (pdf == null ? 0 : pdf.length));
        return pdf;
    }

    /**
     * 시간·요일 분석 행 데이터를 PDF로 생성하여 바이트 배열로 반환한다.
     *
     * <p>
     * 총 건수가 {@link #MAX_PDF_ROWS}를 초과하면 상한까지만 전송하며,
     * 조건 정보에 절단 여부를 포함한다. 빈 데이터인 경우 ReportLab 테이블 구성을 위해
     * 최소 1행을 보정한다.
     * </p>
     *
     * @param cond 조회 조건
     * @return PDF 바이트 배열
     * @throws IllegalStateException PDF 생성 결과가 비었을 때
     */
    @Transactional(readOnly = true)
    public byte[] downloadPdfTime(AnalyticsSearchDto cond) {
        long total = analyticsRepository.countTime(cond);

        boolean truncated = false;
        int fetchSize = (int) Math.min(total, MAX_PDF_ROWS);
        if (total > MAX_PDF_ROWS) truncated = true;

        Pageable fullPage = PageRequest.of(0, Math.max(fetchSize, 1));
        List<TimeRowDto> rawRows = analyticsRepository.findTimeRows(cond, fullPage).getContent();

        if (rawRows.size() > fetchSize && fetchSize > 0) {
            rawRows = rawRows.subList(0, fetchSize);
        }

        List<Map<String, Object>> rows = new ArrayList<>(rawRows.size());
        for (TimeRowDto d : rawRows) {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("date",       nz(d.getDate()));
            m.put("storeName",  nz(d.getStoreName()));
            m.put("hourSlot",   nz(d.getHourSlot()));
            m.put("dayOfWeek",  nz(d.getDayOfWeek()));
            m.put("orderId",    d.getOrderId());
            m.put("orderAmount",d.getOrderAmount());
            m.put("category",   nz(d.getCategory()));
            m.put("menu",       nz(d.getMenu()));
            m.put("orderType",  nz(d.getOrderType()));
            m.put("orderDate",  nz(d.getOrderDate()));
            rows.add(m);
        }
        if (rows.isEmpty()) rows.add(new java.util.LinkedHashMap<>());

        Map<String, Object> criteria = new HashMap<>();
        criteria.put("title", "시간·요일 분석 리포트");
        criteria.put("viewBy", cond.getViewBy() != null ? cond.getViewBy().name() : "DAY");
        criteria.put("startDate", cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "");
        criteria.put("endDate",   cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "");
        criteria.put("rowCount", rows.size());
        criteria.put("totalCount", total);
        criteria.put("truncated", truncated);

        Map<String, Object> payload = new HashMap<>();
        payload.put("criteria", criteria);
        payload.put("data", rows);

        byte[] pdf = pythonPdfClient.generateTimeReportPdf(payload);
        if (pdf == null || pdf.length == 0) {
            throw new IllegalStateException("Empty PDF from /pdf/time");
        }
        log.info("Time PDF ready: total={}, sentRows={}, bytes={}", total, rows.size(), pdf.length);
        return pdf;
    }

    /**
     * null 문자열을 빈 문자열로 치환한다.
     *
     * @param s 입력 문자열
     * @return null이면 빈 문자열, 아니면 원문
     */
    private static String nz(String s) { return (s == null) ? "" : s; }

    /**
     * 헤더 셀 스타일을 생성한다.
     *
     * @param wb 워크북
     * @return 헤더용 셀 스타일(볼드, 중앙정렬, 그레이 배경, 외곽선)
     */
    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 본문(텍스트) 셀 스타일을 생성한다.
     *
     * @param wb 워크북
     * @return 일반 본문용 셀 스타일(얇은 테두리)
     */
    private CellStyle createBodyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * 통화/금액 표현용 셀 스타일을 생성한다.
     *
     * @param wb   워크북
     * @param base 기본 스타일
     * @param df   데이터 포맷
     * @return 통화 포맷 스타일(천단위 구분)
     */
    private CellStyle createMoneyStyle(Workbook wb, CellStyle base, DataFormat df) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(df.getFormat("#,##0"));
        return style;
    }

    /**
     * 정수 표현용 셀 스타일을 생성한다.
     *
     * @param wb   워크북
     * @param base 기본 스타일
     * @param df   데이터 포맷
     * @return 정수 포맷 스타일(천단위 구분)
     */
    private CellStyle createIntegerStyle(Workbook wb, CellStyle base, DataFormat df) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(df.getFormat("#,##0"));
        return style;
    }

    /**
     * 소수 표현용 셀 스타일을 생성한다.
     *
     * @param wb     워크북
     * @param base   기본 스타일
     * @param df     데이터 포맷
     * @param format 소수 포맷 문자열(예: {@code "0.0"})
     * @return 소수 포맷 스타일
     */
    private CellStyle createDecimalStyle(Workbook wb, CellStyle base, DataFormat df, String format) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(df.getFormat(format));
        return style;
    }

    /**
     * 퍼센트 표현용 셀 스타일을 생성한다.
     *
     * @param wb     워크북
     * @param base   기본 스타일
     * @param df     데이터 포맷
     * @param format 퍼센트 포맷 문자열(예: {@code "0.0%"})
     * @return 퍼센트 포맷 스타일
     */
    private CellStyle createPercentStyle(Workbook wb, CellStyle base, DataFormat df, String format) {
        CellStyle style = wb.createCellStyle();
        style.cloneStyleFrom(base);
        style.setDataFormat(df.getFormat(format));
        return style;
    }
    /**
     * 문자열 셀 값을 설정한다.
     *
     * @param row 행
     * @param col 열 인덱스
     * @param val 문자열 값(null 허용)
     * @param st  스타일
     */
    private static void setText(Row row, int col, String val, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        cell.setCellValue(val != null ? val : "");
    }

    /**
     * 숫자 셀 값을 설정한다.
     *
     * @param row 행
     * @param col 열 인덱스
     * @param num 숫자 값(null 허용)
     * @param st  스타일(정수/통화 등)
     */
    private static void setNum(Row row, int col, Number num, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        if (num != null) {
            cell.setCellValue(num.doubleValue());
        }
    }

    /**
     * 퍼센트 셀 값을 설정한다.
     *
     * <p>
     * 입력 값이 1.0보다 크면 100 단위(예: 23.4 ⇒ 0.234)로 간주하여
     * 엑셀 퍼센트 표현과 일치하도록 변환한다.
     * </p>
     *
     * @param row      행
     * @param col      열 인덱스
     * @param pctValue 퍼센트 값(예: 0.234 또는 23.4)
     * @param st       퍼센트 스타일
     */
    private static void setPct(Row row, int col, Number pctValue, CellStyle st) {
        Cell cell = row.createCell(col);
        cell.setCellStyle(st);
        if (pctValue != null) {
            double v = pctValue.doubleValue();
            if (v > 1.0) v = v / 100.0;
            cell.setCellValue(v);
        }
    }
}
