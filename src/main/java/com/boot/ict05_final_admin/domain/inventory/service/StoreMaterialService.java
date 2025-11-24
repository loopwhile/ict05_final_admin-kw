package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.StoreMaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.repository.StoreMaterialRepository;
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
 * 가맹점 재료 도메인 서비스.
 *
 * <p>가맹점 재료 목록 조회 및 엑셀(XLSX) 생성 기능을 제공한다.</p>
 */
@Service
@RequiredArgsConstructor
public class StoreMaterialService {

    private final StoreMaterialRepository storeMaterialRepository;

    /**
     * 가맹점 재료 목록을 페이지 단위로 조회한다.
     *
     * @param searchDTO 검색 조건 DTO(키워드, 상태, 본사 재료 여부, storeId 등)
     * @param pageable  페이지/정렬 정보
     * @return 페이징 처리된 가맹점 재료 리스트 DTO
     */
    @Transactional(readOnly = true)
    public Page<StoreMaterialListDTO> listStoreMaterials(final StoreMaterialSearchDTO searchDTO,
                                                         final Pageable pageable) {
        return storeMaterialRepository.listStoreMaterial(searchDTO, pageable);
    }

    /**
     * 검색 결과 총 건수를 조회한다.
     *
     * @param searchDTO 검색 조건 DTO
     * @return 총 건수
     */
    @Transactional(readOnly = true)
    public long countStoreMaterials(final StoreMaterialSearchDTO searchDTO) {
        return storeMaterialRepository.countStoreMaterial(searchDTO);
    }

    /**
     * 가맹점 재료 목록을 XLSX로 생성한다.
     *
     * <p>
     * 총 건수를 기준으로 단일 페이지로 전체 데이터를 조회한 뒤, 정렬은
     * 컨트롤러에서 전달된 {@code pageable.getSort()}만 반영하여 워크북을 생성한다.
     * 생성 컬럼: CODE, 재료명, 기본단위, 판매단위.
     * 문자열 컬럼은 {@link com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil#n(String)}로 null-safe 처리한다.
     * </p>
     *
     * @param searchDTO 검색 조건 DTO
     * @param pageable  정렬 힌트용 Pageable(페이지/사이즈는 무시)
     * @return 생성된 XLSX 바이트 배열
     * @throws IOException           워크북 쓰기/닫기 중 I/O 오류
     * @throws IllegalStateException 생성 과정의 일반 예외 래핑
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcel(final StoreMaterialSearchDTO searchDTO,
                                final Pageable pageable) throws IOException {
        long total = countStoreMaterials(searchDTO);
        int size = (int) Math.min(Integer.MAX_VALUE, Math.max(1, total));

        Page<StoreMaterialListDTO> page = listStoreMaterials(
                searchDTO, PageRequest.of(0, size, pageable.getSort())
        );

        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("가맹점재료");

            // 헤더
            String[] cols = {"CODE", "재료명", "기본단위", "판매단위"};
            Row header = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            Font bold = wb.createFont();
            bold.setBold(true);
            headerStyle.setFont(bold);
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            // 데이터
            int r = 1;
            for (StoreMaterialListDTO m : page) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(n(m.getCode()));
                row.createCell(1).setCellValue(n(m.getName()));
                row.createCell(2).setCellValue(n(m.getBaseUnit()));
                row.createCell(3).setCellValue(n(m.getSalesUnit()));
                // 필요 시, DTO에 실제로 존재하면 아래 항목을 추가:
                // row.createCell(4).setCellValue(n(String.valueOf(m.getStatus())));
                // row.createCell(5).setCellValue(n(String.valueOf(m.getPurchasePrice())));
                // row.createCell(6).setCellValue(n(String.valueOf(m.getSellingPrice())));
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
            throw new IllegalStateException("가맹점 재료 엑셀 생성 실패", e);
        }
    }
}
