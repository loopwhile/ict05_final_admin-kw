package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.inventory.dto.MaterialListDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialModifyFormDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialSearchDTO;
import com.boot.ict05_final_admin.domain.inventory.dto.MaterialWriteFormDTO;
import com.boot.ict05_final_admin.domain.inventory.entity.Material;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialCategory;
import com.boot.ict05_final_admin.domain.inventory.entity.MaterialStatus;
import com.boot.ict05_final_admin.domain.inventory.repository.InventoryRepository;
import com.boot.ict05_final_admin.domain.inventory.repository.MaterialRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil.n;

/**
 * 본사 재료(Material) 도메인 서비스.
 *
 * <p>
 * 본사 재료 등록/수정/삭제 및 목록 조회, 엑셀 다운로드 기능을 제공한다.
 * 재료 코드 생성, 상태 기본값 부여, 적정 재고량 동기화 등
 * 재료 마스터 관련 비즈니스 로직을 담당한다.
 * </p>
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final InventoryRepository inventoryRepository;

    /**
     * 본사 재료를 등록한다.
     *
     * <p>
     * 카테고리 기반으로 재료 코드를 자동 생성하고, 상태는 기본적으로
     * {@link MaterialStatus#USE}로 저장한다. 적정 재고량(optimalQuantity)은
     * DTO 값이 있으면 해당 값으로, 없으면 엔티티 기본값 정책을 따른다.
     * </p>
     *
     * @param dto 등록 요청 DTO
     * @return 생성된 재료 ID
     */
    @Transactional
    public Long insertOfficeMaterial(final @Valid MaterialWriteFormDTO dto) {
        // 재료 코드 자동 생성(카테고리 필수)
        final String generatedCode = generateMaterialCode(dto.getMaterialCategory());

        final Material material = Material.builder()
                .code(generatedCode) // 생성된 코드 부여
                .materialCategory(dto.getMaterialCategory())
                .name(dto.getName())
                .baseUnit(dto.getBaseUnit())
                .salesUnit(dto.getSalesUnit())
                .conversionRate(dto.getConversionRate())
                .supplier(dto.getSupplier())
                .materialStatus(MaterialStatus.USE)
                .materialTemperature(dto.getMaterialTemperature())
                .optimalQuantity(dto.getOptimalQuantity())
                .regDate(LocalDateTime.now())
                .build();

        final Material saved = materialRepository.save(material);
        return saved.getId();
    }

    /**
     * 재료 목록을 페이지 단위로 조회한다.
     *
     * @param materialSearchDTO 검색 조건 DTO
     * @param pageable          페이지/정렬 정보
     * @return 페이징 처리된 재료 리스트 DTO
     */
    @Transactional(readOnly = true)
    public Page<MaterialListDTO> selectAllMaterial(final MaterialSearchDTO materialSearchDTO,
                                                   final Pageable pageable) {
        return materialRepository.listMaterial(materialSearchDTO, pageable);
    }

    /**
     * ID로 재료 엔티티를 조회한다.
     *
     * @param id 재료 ID
     * @return 재료 엔티티(없으면 null)
     */
    @Transactional(readOnly = true)
    public Material findById(final Long id) {
        return materialRepository.findById(id).orElse(null);
    }

    /**
     * 기존 재료 정보를 수정한다.
     *
     * <p>
     * 대상 재료를 조회하여 {@link Material#updateMaterial(MaterialModifyFormDTO)}로
     * 변경 가능 속성을 갱신한 뒤 저장한다.
     * </p>
     *
     * <p>
     * 적정 재고량(optimalQuantity)의 경우 DTO 값이 null이면
     * 재료 마스터의 기존 값을 유지하며, null이 아닌 경우에는
     * 재료 마스터와 인벤토리(Inventory)의 적정 재고량을 모두 새 값으로
     * 동기화한다.
     * </p>
     *
     * @param dto 수정 요청 DTO
     * @return 수정 후 재료 엔티티
     * @throws IllegalArgumentException 대상 재료가 존재하지 않을 때
     */
    @Transactional
    public Material materialModify(final MaterialModifyFormDTO dto) {
        final Material material = findById(dto.getId());
        if (material == null) {
            throw new IllegalArgumentException("해당 재료가 존재하지 않습니다.");
        }

        // DTO의 null 필드는 엔티티에 덮어쓰지 않도록(널-무시) 구현되어 있어야 함
        material.updateMaterial(dto);
        materialRepository.save(material);

        // 적정 재고만 동기화 (status는 유지)
        // null이면 "미변경"으로 간주 → 동기화 스킵
        if (dto.getOptimalQuantity() != null) {
            inventoryRepository.updateOptimalQuantityByMaterialId(dto.getId(), dto.getOptimalQuantity());
        }

        return material;
    }

    /**
     * 재료 상세 정보를 조회한다.
     *
     * @param id 재료 ID
     * @return 재료 엔티티(없으면 null)
     */
    @Transactional(readOnly = true)
    public Material detailMaterial(final Long id) {
        return materialRepository.findById(id).orElse(null);
    }

    /**
     * 재료를 삭제한다.
     *
     * <p>
     * 삭제 방식(물리/논리 삭제 등)은 리포지토리/엔티티 정책에 따른다.
     * </p>
     *
     * @param id 재료 ID
     */
    @Transactional
    public void deleteMaterial(final Long id) {
        materialRepository.deleteById(id);
    }

    /**
     * 재료 목록을 XLSX로 생성한다.
     *
     * <p>
     * 전체 건수를 조회한 뒤, 단일 페이지로 일괄 조회하여 워크북을 생성한다.
     * 헤더는 본 메서드에서 고정 정의하며, 문자열 컬럼은
     * {@link com.boot.ict05_final_admin.domain.inventory.utility.ExcelUtil#n(String)}
     * 로 null-safe 처리한다.
     * </p>
     *
     * <p>
     * 전달받은 {@link Pageable}은 정렬 힌트로만 사용하고,
     * 실제 쿼리는 전체 행을 한 번에 조회하여 덤프한다.
     * </p>
     *
     * @param materialSearchDTO 검색 조건 DTO
     * @param pageable          스프링 페이징(정렬 힌트용). 페이지/사이즈는 무시하고 전체 덤프
     * @return 생성된 XLSX 파일 바이트 배열
     * @throws IOException           워크북 쓰기/닫기 과정의 I/O 오류
     * @throws IllegalStateException 리포지토리/변환 중 일반 예외 래핑
     */
    @Transactional(readOnly = true)
    public byte[] downloadExcel(final MaterialSearchDTO materialSearchDTO,
                                final Pageable pageable) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            final Sheet sheet = wb.createSheet("재료목록");

            // Header
            final String[] cols = {"ID", "CODE", "카테고리", "재료명", "기본단위", "판매단위", "공급업체", "상태"};
            final Row h = sheet.createRow(0);
            final CellStyle hs = wb.createCellStyle();
            final Font f = wb.createFont();
            f.setBold(true);
            hs.setFont(f);
            for (int i = 0; i < cols.length; i++) {
                final Cell c = h.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(hs);
            }

            // Data
            final long total = materialRepository.countMaterial(materialSearchDTO);
            final int size = (int) Math.min(Integer.MAX_VALUE, total);
            final PageRequest p0 = PageRequest.of(0, Math.max(1, size), Sort.by("id").descending());
            final Page<MaterialListDTO> page = materialRepository.listMaterial(materialSearchDTO, p0);

            int r = 1;
            for (MaterialListDTO m : page) {
                final Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(m.getId());
                row.createCell(1).setCellValue(n(m.getCode()));
                row.createCell(2).setCellValue(String.valueOf(m.getMaterialCategory()));
                row.createCell(3).setCellValue(n(m.getName()));
                row.createCell(4).setCellValue(n(m.getBaseUnit()));
                row.createCell(5).setCellValue(n(m.getSalesUnit()));
                row.createCell(6).setCellValue(n(m.getSupplier()));
                row.createCell(7).setCellValue(String.valueOf(m.getMaterialStatus()));
            }

            // Autosize
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(bos);
            return bos.toByteArray();
        } catch (IOException ioe) {
            throw ioe; // 체크 예외는 원인 보존하여 전파
        } catch (Exception e) {
            throw new IllegalStateException("재료 목록 엑셀 생성 실패", e);
        }
    }

    /**
     * 카테고리 기반 재료 코드 생성.
     *
     * <p>
     * {@link MaterialCategory#getCodePrefix()}를 접두사로 사용하고,
     * 리포지토리에서 해당 카테고리의 최대 코드를 조회해 다음 일련번호를 산정한다.
     * 접두사 길이는 카테고리 정책을 따른다(현재 3자 접두사 전제).
     * </p>
     *
     * @param category 재료 카테고리
     * @return 생성된 코드(예: {@code BAS0001})
     */
    private String generateMaterialCode(final MaterialCategory category) {
        final String prefix = category.getCodePrefix(); // Enum에서 제공
        final String lastCode = materialRepository.findMaxCodeByCategory(category);
        int nextNum = 1;

        if (lastCode != null && lastCode.length() >= prefix.length()) {
            try {
                // 접두사 뒤 일련번호만 파싱
                nextNum = Integer.parseInt(lastCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                log.warn("잘못된 코드 형식: {}", lastCode);
            }
        }

        return String.format("%s%04d", prefix, nextNum);
    }

    /**
     * 카테고리별 재료 목록을 조회한다.
     *
     * <p>본사 입고 등록 시, 선택 카테고리의 사용중(USE) 재료만 반환한다.</p>
     *
     * @param category 재료 카테고리(예: BASE, SAUCE 등)
     * @return 조건에 맞는 재료 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<MaterialListDTO> findByCategory(final MaterialCategory category) {
        return materialRepository.findByCategory(category)
                .stream()
                .map(MaterialListDTO::new)
                .collect(Collectors.toList());
    }
}
