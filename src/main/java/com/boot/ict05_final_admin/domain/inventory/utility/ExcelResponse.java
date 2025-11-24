package com.boot.ict05_final_admin.domain.inventory.utility;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * XLSX 바이너리 응답 헬퍼.
 *
 * <p>Content-Disposition에 RFC 5987 기반 filename*를 함께 설정해
 * 한글 파일명 등 비ASCII 파일명 다운로드 호환성을 확보한다.</p>
 *
 * <ul>
 *   <li>Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet</li>
 *   <li>Content-Disposition: attachment; filename="ASCII-fallback"; filename*=UTF-8''{url-encoded}</li>
 *   <li>Cache-Control/Pragma: no-cache</li>
 * </ul>
 *
 * @author 김주연
 * @since 2025-11-12
 */
public final class ExcelResponse {

    private ExcelResponse() {}

    /**
     * XLSX 바이트 배열을 다운로드 응답으로 래핑한다.
     *
     * @param bytes    XLSX 바이트 배열(널 불가)
     * @param filename 다운로드 파일명(원문; fallback/filename* 모두 생성)
     * @return ResponseEntity<byte[]>
     * @throws IllegalArgumentException bytes가 null이거나 비어 있는 경우
     */
    public static ResponseEntity<byte[]> ok(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("bytes must not be null or empty");
        }

        String fallback = ExcelFilename.fallbackAscii(filename);
        String encoded = ExcelFilename.encodeRFC5987(filename);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CACHE_CONTROL, "no-cache");
        headers.set(HttpHeaders.PRAGMA, "no-cache");
        headers.set(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fallback + "\"; filename*=UTF-8''" + encoded
        );

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bytes.length)
                .body(bytes);
    }
}
