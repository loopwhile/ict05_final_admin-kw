package com.boot.ict05_final_admin.domain.inventory.utility;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 엑셀 다운로드 파일명 생성 유틸 v1.0
 *
 * <ul>
 *   <li>타임스탬프: yyyyMMddHHmmss</li>
 *   <li>본사 목록: 한글 프리픽스 고정</li>
 *   <li>가맹점 목록: (전체가맹점 | 가맹점명)_접미사_YYMMDDHHMMSS.xlsx</li>
 *   <li>재고 로그/배치: 재고로그_재료명_yyyyMMddHHmmss.xlsx, 재고배치_재료명_yyyyMMddHHmmss.xlsx</li>
 * </ul>
 */
@UtilityClass
public class ExcelFilename {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /* ================= HQ 목록 ================= */

    public static String hqMaterial() {
        return "재료목록_" + now() + ".xlsx";
    }

    public static String hqInventory() {
        return "재고목록_" + now() + ".xlsx";
    }

    /* ================= 가맹점 목록 ================= */

    public static String storeMaterial(String storeNameOrNull) {
        String head = (storeNameOrNull == null || storeNameOrNull.isBlank())
                ? "전체가맹점"
                : storeNameOrNull;
        // 전체가맹점or가맹점명_재료목록_YYMMDDHHMMSS.xlsx
        return sanitize(head + "_재료목록_" + shortNow() + ".xlsx");
    }

    public static String storeInventory(String storeNameOrNull) {
        String head = (storeNameOrNull == null || storeNameOrNull.isBlank())
                ? "전체가맹점"
                : storeNameOrNull;
        // 전체가맹점or가맹점명_재고목록_YYMMDDHHMMSS.xlsx
        return sanitize(head + "_재고목록_" + shortNow() + ".xlsx");
    }

    /* ================= 로그/배치 – 이름 기반 권장 ================= */

    /** @deprecated ID 포함 파일명은 단계적으로 폐기. 이름 기반을 사용 권장. */
    @Deprecated
    public static String inventoryLog(long materialId) {
        return "재고로그_" + materialId + "_" + now() + ".xlsx";
    }

    public static String inventoryLogByName(String materialNameOrNull) {
        String head = (materialNameOrNull == null || materialNameOrNull.isBlank())
                ? "재고로그"
                : sanitize("재고로그_" + materialNameOrNull);
        // 재고로그_재료명_yyyyMMddHHmmss.xlsx
        return sanitize(head + "_" + now() + ".xlsx");
    }

    /** @deprecated ID 포함 파일명은 단계적으로 폐기. 이름 기반을 사용 권장. */
    @Deprecated
    public static String inventoryBatch(long materialId) {
        return "재고배치_" + materialId + "_" + now() + ".xlsx";
    }

    public static String inventoryBatchByName(String materialNameOrNull) {
        String head = (materialNameOrNull == null || materialNameOrNull.isBlank())
                ? "재고배치"
                : sanitize("재고배치_" + materialNameOrNull);
        // 재고배치_재료명_yyyyMMddHHmmss.xlsx
        return sanitize(head + "_" + now() + ".xlsx");
    }

    /**
     * LOT 출고 이력 엑셀 파일명
     * 예: LOT-251115-927999_출고내역_20251117113000.xlsx
     */
    public static String inventoryLotOutHistoryByLotNo(String lotNo) {
        String prefix = (lotNo == null || lotNo.isBlank())
                ? "LOT출고내역_"
                : lotNo + "_출고내역_";
        return sanitize(prefix + now() + ".xlsx");
    }

    /* ================= 헤더 인코딩 보조 ================= */

    /** RFC 5987 형식의 filename* 값 생성을 위한 URL 인코딩(UTF-8, 공백은 %20) */
    public static String encodeRFC5987(@NonNull String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** Content-Disposition의 ASCII fallback 값 생성 */
    public static String fallbackAscii(@NonNull String filename) {
        String base = filename.replaceAll("[^A-Za-z0-9._()-]", "_");
        return base.isBlank() ? "download.xlsx" : base;
    }

    /* ================= 내부 유틸 ================= */

    private static String now() {
        return LocalDateTime.now().format(TS);
    }

    /** YYMMDDHHMMSS 형식의 짧은 타임스탬프 */
    private static String shortNow() {
        String full = now(); // yyyyMMddHHmmss
        return full.substring(2);
    }

    /** 파일 시스템 금지 문자를 제거한 안전한 파일명으로 정리 */
    private static String sanitize(String name) {
        // Windows 금지 문자 제거: \ / : * ? " < > |
        return name.replaceAll("[\\\\/:*?\"<>|]", "");
    }
}
