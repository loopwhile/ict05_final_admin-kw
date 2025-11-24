package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeStatus;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 공지사항 등록 폼 DTO
 *
 * <p>공지사항 등록 시 클라이언트에서 전달되는 데이터 전송 객체(DTO)이다.
 * 공지사항의 카테고리, 우선순위, 공개여부, 작성자, 제목, 내용 및 첨부파일 리스트를 포함한다.</p>
 *
 * <p>검증 어노테이션:</p>
 * <ul>
 *     <li>@NotNull: 카테고리, 우선순위 필수</li>
 *     <li>@NotBlank: 작성자, 제목, 내용 필수</li>
 * </ul>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li>noticeCategory: 공지사항 카테고리 (필수)</li>
 *     <li>noticePriority: 공지사항 우선순위 (필수)</li>
 *     <li>isShow: 공지사항 공개 여부</li>
 *     <li>writer: 작성자 이름 (필수)</li>
 *     <li>title: 공지사항 제목 (필수)</li>
 *     <li>body: 공지사항 내용 (필수)</li>
 *     <li>files: 첨부파일 리스트 (없을 수 있음)</li>
 * </ul>
 */
@Data
public class NoticeWriteFormDTO {

    /** 작성자(회원) FK */
    private Long memberIdFk;

    /** 공지사항 카테고리 (필수) */
    @NotNull(message = "카테고리를 선택해주세요")
    private NoticeCategory noticeCategory;

    /** 공지사항 우선순위 (필수) */
    @NotNull(message = "상태를 선택해주세요")
    private NoticePriority noticePriority;

    /** 공지사항 상태  */
    private NoticeStatus noticeStatus;

    /** 공지사항 공개 여부 */
    private Boolean isShow;

    /** 작성자 이름 (필수) */
    @NotBlank(message = "작성자를 입력해주세요")
    private String writer;

    /** 공지사항 제목 (필수) */
    @NotBlank(message = "제목을 입력해주세요")
    private String title;

    /** 공지사항 내용 (필수) */
    @NotBlank(message = "내용을 입력해주세요")
    private String body;

    /** 첨부파일 리스트 (없을 수 있음) */
    @Parameter(hidden = true)
    private List<MultipartFile> files;
}
