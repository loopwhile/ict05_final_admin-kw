package com.boot.ict05_final_admin.domain.analytics.controller;

import com.boot.ict05_final_admin.domain.analytics.dto.AnalyticsSearchDto;
import com.boot.ict05_final_admin.domain.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

/**
 * 통계/리포트 다운로드 REST 컨트롤러.
 *
 * <p>
 * KPI, 주문, 시간·요일 분석 영역의 <b>엑셀(XLSX)</b> 및 <b>PDF</b> 다운로드 엔드포인트를 제공한다.
 * 모든 엔드포인트는 요청 파라미터로 전달된 {@link AnalyticsSearchDto} 조건을 바탕으로
 * 서버에서 파일 바이트를 생성하여 <code>Content-Disposition: attachment</code> 헤더와 함께 반환한다.
 * </p>
 *
 * <h3>다운로드 공통 규칙</h3>
 * <ul>
 *   <li>파일명: 기능_기간(or 모드)_yyyy-MM-dd 형식으로 구성</li>
 *   <li>인코딩: RFC5987 형식의 <code>filename*=UTF-8''...</code> 로 브라우저 호환성 확보</li>
 *   <li>캐시: <code>Cache-Control: no-cache</code> 적용</li>
 * </ul>
 *
 * @author 이경욱
 * @since 2025.10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API/analytics")
@Tag(name = "통계/리포트 API", description = "KPI/주문/시간 분석 리포트의 엑셀·PDF 다운로드 제공")
public class AnalyticsRestController {

	private static final String XLSX_MIME = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	private final AnalyticsService analyticsService;

	/**
	 * KPI 리스트 엑셀 다운로드.
	 *
	 * <p>조회조건(가맹점, 기간 등)에 맞는 KPI 테이블을 생성하여 XLSX 바이트로 반환한다.</p>
	 *
	 * @param cond     KPI 조회조건(가맹점/기간/출력방식 등)
	 * @param pageable 페이지 사이즈(엑셀 행 개수 제한 등에 활용)
	 * @return XLSX 파일 바이트 리소스 (Content-Disposition 첨부)
	 */
	@Operation(
			summary = "KPI 엑셀 다운로드",
			description = "조건(가맹점/기간/출력방식)에 맞는 KPI 리스트를 XLSX 파일로 다운로드합니다."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "다운로드 성공",
					content = @Content(mediaType = XLSX_MIME)),
			@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping(value = "/kpi/download", produces = XLSX_MIME)
	public ResponseEntity<Resource> downloadExcelKpiList(
			@ParameterObject @ModelAttribute AnalyticsSearchDto cond,
			@ParameterObject Pageable pageable
	) {
		byte[] excelBytes = analyticsService.downloadExcelKpi(cond, pageable);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String filename = "KPI_" + start + "_" + end + ".xlsx";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.parseMediaType(XLSX_MIME))
				.body(new ByteArrayResource(excelBytes));
	}

	/**
	 * 주문 리스트 엑셀 다운로드.
	 *
	 * <p>조건에 맞는 주문/메뉴 집계를 생성하여 XLSX 바이트로 반환한다.</p>
	 *
	 * @param cond     주문 분석 조회조건
	 * @param pageable 페이지/사이즈 정보
	 * @return XLSX 파일 바이트 리소스
	 */
	@Operation(
			summary = "주문 엑셀 다운로드",
			description = "조건(가맹점/기간/출력방식)에 맞는 주문 리스트를 XLSX 파일로 다운로드합니다."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "다운로드 성공",
					content = @Content(mediaType = XLSX_MIME)),
			@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping(value = "/orders/download", produces = XLSX_MIME)
	public ResponseEntity<Resource> downloadExcelOrdersList(
			@ParameterObject @ModelAttribute AnalyticsSearchDto cond,
			@ParameterObject Pageable pageable
	) {
		byte[] excelBytes = analyticsService.downloadExcelOrders(cond, pageable);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String filename = "Orders_" + start + "_" + end + ".xlsx";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.parseMediaType(XLSX_MIME))
				.body(new ByteArrayResource(excelBytes));
	}

	/**
	 * 시간·요일 분석 엑셀 다운로드.
	 *
	 * <p>선택 기간의 시간대/요일별 매출·주문 분석 결과를 XLSX 바이트로 반환한다.</p>
	 *
	 * @param cond     시간·요일 분석 조회조건(특히 viewBy: DAY/MONTH)
	 * @param pageable 페이지/사이즈 정보
	 * @return XLSX 파일 바이트 리소스
	 */
	@Operation(
			summary = "시간·요일 분석 엑셀 다운로드",
			description = "조건(가맹점/기간/일·월 모드)에 맞게 시간·요일 분석 결과를 XLSX로 다운로드합니다."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "다운로드 성공",
					content = @Content(mediaType = XLSX_MIME)),
			@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping(value = "/time/download", produces = XLSX_MIME)
	public ResponseEntity<Resource> downloadExcelTimeList(
			@ParameterObject @ModelAttribute AnalyticsSearchDto cond,
			@ParameterObject Pageable pageable
	) {
		byte[] excelBytes = analyticsService.downloadExcelTime(cond, pageable);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String mode  = cond.getViewBy() != null ? cond.getViewBy().name() : "DAY";
		String filename = "Time_" + mode + "_" + start + "_" + end + ".xlsx";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.parseMediaType(XLSX_MIME))
				.body(new ByteArrayResource(excelBytes));
	}

	/**
	 * 재료 리스트 엑셀 다운로드.
	 *
	 * <p>조건에 맞는 재료 사용/발주/마진율 집계를 생성하여 XLSX 바이트로 반환한다.</p>
	 *
	 * @param cond     재료 분석 조회조건
	 * @param pageable 페이지/사이즈 정보
	 * @return XLSX 파일 바이트 리소스
	 */
	@Operation(
			summary = "재료 엑셀 다운로드",
			description = "조건(가맹점/기간/출력방식)에 맞는 재료 리스트를 XLSX 파일로 다운로드합니다."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "다운로드 성공",
					content = @Content(mediaType = XLSX_MIME)),
			@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping(value = "/materials/download", produces = XLSX_MIME)
	public ResponseEntity<Resource> downloadExcelMaterialsList(
			@ParameterObject @ModelAttribute AnalyticsSearchDto cond,
			@ParameterObject Pageable pageable
	) {
		byte[] excelBytes = analyticsService.downloadExcelMaterials(cond, pageable);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String filename = "Materials_" + start + "_" + end + ".xlsx";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.parseMediaType(XLSX_MIME))
				.body(new ByteArrayResource(excelBytes));
	}

	/**
	 * KPI 리스트 PDF 다운로드.
	 *
	 * <p>조회조건으로 생성된 KPI 리포트를 PDF 바이트로 반환한다.</p>
	 *
	 * @param cond KPI 조회조건
	 * @return PDF 파일 바이트 리소스
	 */
	@Operation(
			summary = "KPI PDF 다운로드",
			description = "조건(가맹점/기간/출력방식)에 맞는 KPI 리포트를 PDF로 다운로드합니다."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "다운로드 성공",
					content = @Content(mediaType = "application/pdf")),
			@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping(value = "/kpi/pdf/download", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<Resource> downloadPdfKpiList(
			@ParameterObject @ModelAttribute AnalyticsSearchDto cond
	) {
		byte[] pdfBytes = analyticsService.downloadPdfKpi(cond);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String filename = "KPI_" + start + "_" + end + ".pdf";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.APPLICATION_PDF)
				.body(new ByteArrayResource(pdfBytes));
	}

	/**
	 * 주문 리스트 PDF 다운로드.
	 *
	 * <p>주문/메뉴 분석 리포트를 PDF 바이트로 반환한다.</p>
	 *
	 * @param cond 주문 분석 조회조건
	 * @return PDF 파일 바이트 리소스
	 */
	@Operation(
			summary = "주문 PDF 다운로드",
			description = "조건(가맹점/기간/일·월 모드)에 맞는 주문 분석 리포트를 PDF로 다운로드합니다."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "다운로드 성공",
					content = @Content(mediaType = "application/pdf")),
			@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping(value = "/orders/pdf/download", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<Resource> downloadPdfOrders(
			@ParameterObject @ModelAttribute AnalyticsSearchDto cond
	) {
		byte[] pdfBytes = analyticsService.downloadPdfOrders(cond);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String mode  = cond.getViewBy() != null ? cond.getViewBy().name() : "DAY";
		String filename = "Orders_" + mode + "_" + start + "_" + end + ".pdf";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.APPLICATION_PDF)
				.body(new ByteArrayResource(pdfBytes));
	}

	/**
	 * 시간·요일 분석 PDF 다운로드.
	 *
	 * <p>시간대/요일 차트를 포함한 분석 리포트를 PDF 바이트로 반환한다.</p>
	 *
	 * @param cond 시간·요일 분석 조회조건(특히 viewBy: DAY/MONTH)
	 * @return PDF 파일 바이트 리소스
	 */
	@Operation(
			summary = "시간·요일 분석 PDF 다운로드",
			description = "조건(가맹점/기간/일·월 모드)에 맞는 시간·요일 분석 리포트를 PDF로 다운로드합니다."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "다운로드 성공",
					content = @Content(mediaType = "application/pdf")),
			@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping(value = "/time/pdf/download", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<Resource> downloadPdfTime(
			@ParameterObject @ModelAttribute AnalyticsSearchDto cond
	) {
		byte[] pdfBytes = analyticsService.downloadPdfTime(cond);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String mode  = cond.getViewBy() != null ? cond.getViewBy().name() : "DAY";
		String filename = "Time_" + mode + "_" + start + "_" + end + ".pdf";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.APPLICATION_PDF)
				.body(new ByteArrayResource(pdfBytes));
	}

	/**
	 * 재료 리스트 PDF 다운로드.
	 *
	 * <p>재료 분석 리포트를 PDF 바이트로 반환한다.</p>
	 *
	 * @param cond 재료 분석 조회조건
	 * @return PDF 파일 바이트 리소스
	 */
	@Operation(
			summary = "재료 PDF 다운로드",
			description = "조건(가맹점/기간/일·월 모드)에 맞는 재료 분석 리포트를 PDF로 다운로드합니다."
	)
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "다운로드 성공",
					content = @Content(mediaType = "application/pdf")),
			@ApiResponse(responseCode = "500", description = "서버 오류")
	})
	@GetMapping(value = "/materials/pdf/download", produces = MediaType.APPLICATION_PDF_VALUE)
	public ResponseEntity<Resource> downloadPdfMaterials(
			@ParameterObject @ModelAttribute AnalyticsSearchDto cond
	) {
		byte[] pdfBytes = analyticsService.downloadPdfMaterials(cond);

		String start = cond.getStartDate() != null ? cond.getStartDate().format(DateTimeFormatter.ISO_DATE) : "start";
		String end   = cond.getEndDate()   != null ? cond.getEndDate().format(DateTimeFormatter.ISO_DATE)   : "end";
		String mode  = cond.getViewBy() != null ? cond.getViewBy().name() : "DAY";
		String filename = "Materials_" + mode + "_" + start + "_" + end + ".pdf";
		String encoded  = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+","%20");

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
				.header(HttpHeaders.CACHE_CONTROL, "no-cache")
				.contentType(MediaType.APPLICATION_PDF)
				.body(new ByteArrayResource(pdfBytes));
	}
}
