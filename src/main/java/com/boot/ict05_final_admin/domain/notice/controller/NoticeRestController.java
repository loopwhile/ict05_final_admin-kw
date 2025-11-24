package com.boot.ict05_final_admin.domain.notice.controller;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeModifyFormDTO;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeSearchDTO;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import com.boot.ict05_final_admin.domain.notice.service.NoticeAttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeWriteFormDTO;
import com.boot.ict05_final_admin.domain.notice.entity.Notice;
import com.boot.ict05_final_admin.domain.notice.service.NoticeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 공지사항 관련 REST API 컨트롤러
 *
 * <p>이 컨트롤러는 다음과 같은 기능을 제공합니다:</p>
 * <ul>
 *     <li>공지사항 등록</li>
 *     <li>공지사항 수정</li>
 * </ul>
 *
 * <p>요청 시 첨부파일 업로드를 지원하며,
 * {@link NoticeWriteFormDTO}, {@link NoticeModifyFormDTO} 를 통해
 * 검증 및 데이터 바인딩을 수행합니다.</p>
 *
 * @author ICT
 * @since 2025.10
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/API")
@Tag(name = "공지사항 API", description = "공지사항 등록/조회/수정/삭제 기능 제공")
@Slf4j
public class NoticeRestController {

    private final NoticeService noticeService;
    private final NoticeAttachmentService noticeAttachmentService;

    /**
     * 공지사항 등록 API
     *
     * <p>본사에서 새로운 공지사항을 등록하는 엔드포인트입니다.
     * 첨부파일을 포함한 공지사항 데이터를 저장합니다.</p>
     *
     * @param dto 등록할 공지사항 데이터 (제목, 내용, 카테고리, 우선순위, 첨부파일 포함)
     * @param bindingResult 유효성 검증 결과
     * @return 등록 성공 여부 및 생성된 공지사항 ID
     * @throws Exception 파일 처리 오류 또는 DB 저장 오류
     */
    @PostMapping("/notice/write")
    @Operation(
            summary = "공지사항 등록",
            description = "본사에서 새로운 공지사항을 등록하는 API입니다. 첨부파일도 함께 업로드 가능합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공지사항 등록 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "등록 성공",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json"
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "검증 오류 발생"
                    )
            }
    )
    public ResponseEntity<Map<String, Object>> addOfficeNotice(
            @Validated @ModelAttribute NoticeWriteFormDTO dto,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        Long id = noticeService.insertOfficeNotice(dto, dto.getFiles());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }

    /**
     * 공지사항 수정 API
     *
     * <p>기존 공지사항을 수정하는 엔드포인트입니다.
     * 첨부파일 변경/추가도 함께 지원합니다.</p>
     *
     * @param dto 수정할 공지사항 데이터 (제목, 내용, 카테고리, 우선순위, 첨부파일 포함)
     * @param bindingResult 유효성 검증 결과
     * @return 수정 성공 여부 및 수정된 공지사항 ID
     * @throws Exception 파일 처리 오류 또는 DB 저장 오류
     */
    @PostMapping("/notice/modify")
    @Operation(
            summary = "공지사항 수정",
            description = "기존 공지사항을 수정하는 API입니다. 첨부파일 변경/추가도 가능합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "공지사항 수정 정보",
                    required = true
            ),
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
                            content = @io.swagger.v3.oas.annotations.media.Content(
                                    mediaType = "application/json"
                            )
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "400",
                            description = "검증 오류 발생"
                    )
            }
    )
    public ResponseEntity<Map<String, Object>> boardModify(
            @Validated @ModelAttribute NoticeModifyFormDTO dto,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            fieldError -> fieldError.getField(),
                            fieldError -> fieldError.getDefaultMessage()
                    ));

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "success", false,
                            "errors", errors
                    ));
        }

        Long id = noticeService.noticeModify(dto, dto.getFiles()).getId();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }


    @PostMapping("/notice/delete/attachment")
    public ResponseEntity<Map<String, Object>> deleteAttachment(@RequestParam("id") Long id) {
        noticeAttachmentService.deleteAttachment(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(Map.of(
                        "success", true,
                        "id", id
                ));
    }


    @GetMapping("/notice/download")
    public ResponseEntity<?> downloadNotice(NoticeSearchDTO noticeSearchDTO,
                                            Pageable pageable) throws IOException {

        byte[] excelBytes = noticeService.downloadExcel(noticeSearchDTO, pageable);

        String filename = "공지사항.xlsx";
        String encodeFilename = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + encodeFilename);
        headers.add("Cache-Control", "no-cache");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

}
