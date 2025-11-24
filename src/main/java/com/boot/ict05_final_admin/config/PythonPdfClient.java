package com.boot.ict05_final_admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * FastAPI 기반 PDF 생성 서버와의 통신을 담당하는 WebClient 클라이언트 컴포넌트.
 *
 * <p>
 * 본 클래스는 Spring WebFlux의 {@link WebClient}를 사용하여
 * Python FastAPI 서버의 PDF 생성 엔드포인트에 JSON 페이로드를 POST 요청으로 전달하고,
 * 응답으로 수신된 PDF 바이트를 반환한다.
 * </p>
 *
 * <h3>주요 특징</h3>
 * <ul>
 *   <li>PDF 서버 Base URL은 {@code pdf.python.base-url} 프로퍼티에서 주입됨</li>
 *   <li>기본 메모리 버퍼 크기: 64MB (대용량 PDF 지원)</li>
 *   <li>응답이 0바이트일 경우 예외 발생 (조용한 실패 방지)</li>
 *   <li>에러 응답(4xx, 5xx)은 {@link IllegalStateException}으로 전파</li>
 * </ul>
 *
 * <h3>지원 엔드포인트</h3>
 * <ul>
 *   <li>{@code /pdf/contract} — 계약서 PDF</li>
 *   <li>{@code /pdf/orders} — 주문 분석 리포트 PDF</li>
 *   <li>{@code /pdf/time} — 시간·요일 분석 리포트 PDF</li>
 *   <li>{@code /pdf/kpi-report} — KPI 리포트 PDF</li>
 * </ul>
 *
 * <p>이 클래스는 Thread-safe 하며, 싱글턴 빈으로 동작한다.</p>
 *
 * @author 이경욱
 * @since 2025.10
 */
@Component
@Slf4j
public class PythonPdfClient {

    private final WebClient pdfWebClient;

    /**
     * PDF 서버와 통신하기 위한 {@link WebClient}를 초기화한다.
     *
     * @param baseUrl FastAPI 서버의 기본 URL (예: {@code http://localhost:8000})
     */
    @Autowired
    public PythonPdfClient(@Value("${pdf.python.base-url}") String baseUrl) {
        this.pdfWebClient = WebClient.builder()
                .baseUrl(baseUrl)
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(c -> c.defaultCodecs().maxInMemorySize(64 * 1024 * 1024)) // 64MB 제한
                                .build()
                )
                .build();
    }

    /**
     * 계약서 PDF를 생성한다.
     *
     * @param payload 계약서 생성 요청 페이로드
     * @return PDF 바이트 배열
     * @throws IllegalStateException FastAPI 서버 오류 또는 빈 응답 발생 시
     */
    public byte[] generateContractPdf(Object payload) {
        return postPdfOrThrow("/pdf/contract", payload);
    }

    /**
     * 주문 분석 리포트 PDF를 생성한다.
     *
     * @param payload 주문 분석 요청 페이로드
     * @return PDF 바이트 배열
     * @throws IllegalStateException FastAPI 서버 오류 또는 빈 응답 발생 시
     */
    public byte[] generateOrdersReportPdf(Object payload) {
        return postPdfOrThrow("/pdf/orders", payload);
    }

    /**
     * 시간·요일 분석 리포트 PDF를 생성한다.
     *
     * @param payload 시간 분석 요청 페이로드
     * @return PDF 바이트 배열
     * @throws IllegalStateException FastAPI 서버 오류 또는 빈 응답 발생 시
     */
    public byte[] generateTimeReportPdf(Object payload) {
        return postPdfOrThrow("/pdf/time", payload);
    }

    /**
     * KPI 분석 리포트 PDF를 생성한다.
     *
     * @param payload KPI 리포트 요청 페이로드
     * @return PDF 바이트 배열
     * @throws IllegalStateException FastAPI 서버 오류 또는 빈 응답 발생 시
     */
    public byte[] generateKpiReportPdf(Object payload) {
        return postPdfOrThrow("/pdf/kpi-report", payload);
    }

    /**
     * 재료 분석 리포트 PDF를 생성한다.
     *
     * @param payload 재료 분석 요청 페이로드
     * @return PDF 바이트 배열
     * @throws IllegalStateException FastAPI 서버 오류 또는 빈 응답 발생 시
     */
    public byte[] generateMaterialsReportPdf(Object payload) {
        return postPdfOrThrow("/pdf/materials", payload);
    }

    /**
     * 공통 POST 요청 메서드.
     *
     * <p>
     * 지정된 경로(path)에 JSON 본문을 POST 전송하고, 응답을 PDF 바이트로 수신한다.
     * 응답이 오류 상태(4xx, 5xx)이거나 0바이트인 경우 명시적으로 예외를 던진다.
     * </p>
     *
     * @param path 요청 경로 (예: {@code /pdf/orders})
     * @param body 전송할 JSON 객체
     * @return 응답 PDF 바이트 배열
     * @throws IllegalStateException FastAPI 서버 오류 또는 응답이 비어 있을 때
     */
    private byte[] postPdfOrThrow(String path, Object body) {
        byte[] bytes = pdfWebClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_PDF)
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(msg -> {
                                    log.error("PDF server error {} {}: {}", resp.statusCode().value(), path, msg);
                                    return Mono.error(new IllegalStateException(
                                            "PDF server error %s: %s".formatted(resp.statusCode(), msg)));
                                })
                )
                .bodyToMono(byte[].class)
                .block();

        int len = (bytes == null ? 0 : bytes.length);
        log.info("PDF fetched from {} bytes={}", path, len);

        if (len == 0) {
            throw new IllegalStateException("Empty PDF from " + path);
        }
        return bytes;
    }
}
