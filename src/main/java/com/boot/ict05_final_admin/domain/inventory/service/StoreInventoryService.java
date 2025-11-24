package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventoryListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreInventorySearchDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.StoreInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil.n;

/**
 * 가맹점 재고 도메인 서비스.
 *
 * <p>조회 전용 컴포넌트(수정/삭제 로직 없음).</p>
 */
@Service
@RequiredArgsConstructor
public class StoreInventoryService {

    private final StoreInventoryRepository storeInventoryRepository;

    /**
     * 가맹점 재고 목록을 페이지 단위로 조회한다.
     *
     * @param searchDTO 검색 조건 DTO
     * @param pageable  페이지/정렬 정보
     * @return 페이징 처리된 가맹점 재고 리스트 DTO
     */
    @Transactional(readOnly = true)
    public Page<StoreInventoryListDTO> listStoreInventory(final StoreInventorySearchDTO searchDTO,
                                                          final Pageable pageable) {
        return storeInventoryRepository.listStoreInventory(searchDTO, pageable);
    }

    /**
     * 가맹점 재고 총 건수를 조회한다.
     *
     * @param searchDTO 검색 조건 DTO
     * @return 총 건수
     */
    @Transactional(readOnly = true)
    public long countStoreInventory(final StoreInventorySearchDTO searchDTO) {
        return storeInventoryRepository.countStoreInventory(searchDTO);
    }

    /**
     * 가맹점 재고 목록을 XLSX로 생성한다.
     *
     * <p>
     * 전체 건수를 조회한 뒤, 정렬 힌트를 유지하기 위해 전달된 {@code pageable.getSort()}를 사용하여
     * 단일 페이지로 전체 데이터를 조회하고 워크북을 생성한다.
     * </p>
     *
     * <p>
     * 생성 컬럼: 재료명, 현재수량, 적정수량, 상태, 갱신일시.<br>
     * 문자열 컬럼은 {@link com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil#n(String)} 로 null-safe 처리한다.
     * </p>
     *
     * @param searchDTO 검색 조건 DTO
     * @param pageable  정렬 힌트용 Pageable(페이지/사이즈 무시, 정렬만 사용)
     * @return 생성된 XLSX 파일 바이트 배열
     * @throws IOException           워크북 쓰기/닫기 중 I/O 오류
     * @throws IllegalStateException XLSX 생성 과정의 일반 예외 래핑
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcel(final StoreInventorySearchDTO searchDTO,
                                final Pageable pageable) throws IOException {
        long total = countStoreInventory(searchDTO);
        int size = (int) Math.min(Integer.MAX_VALUE, Math.max(1, total));

        Page<StoreInventoryListDTO> page = listStoreInventory(
                searchDTO, PageRequest.of(0, size, pageable.getSort())
        );

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("가맹점재고");

            // 헤더
            String[] cols = {"재료명", "현재수량", "적정수량", "상태", "갱신일시"};
            Row header = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            // 본문
            int r = 1;
            for (StoreInventoryListDTO v : page) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(n(v.getMaterialName()));
                row.createCell(1).setCellValue(v.getQuantity() == null ? 0d : v.getQuantity().doubleValue());
                row.createCell(2).setCellValue(v.getOptimalQuantity() == null ? "" : String.valueOf(v.getOptimalQuantity()));
                row.createCell(3).setCellValue(v.getStatus() == null ? "" : String.valueOf(v.getStatus()));
                row.createCell(4).setCellValue(v.getUpdateDate() == null ? "" : v.getUpdateDate().toString());

                // 필요 시, DTO에 실제로 존재하면 아래 항목 추가:
                // row.createCell(5).setCellValue(n(v.getMaterialSalesUnit()));
                // row.createCell(6).setCellValue(v.getMaterialId() == null ? "" : String.valueOf(v.getMaterialId()));
                // row.createCell(7).setCellValue(n(v.getStoreName()));
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException ioe) {
            throw ioe; // 체크 예외는 원인 보존하여 전파
        } catch (Exception e) {
            throw new IllegalStateException("가맹점 재고 엑셀 생성 실패", e);
        }
    }
}
