package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공지사항 상세 정보 DTO
 *
 * <p>공지사항 상세 조회 시 사용되는 데이터 전송 객체(DTO)이다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li>id: 공지사항 고유 ID</li>
 *     <li>noticeCategory: 공지사항 카테고리</li>
 *     <li>noticePriority: 공지사항 우선순위</li>
 *     <li>isShow: 공지사항 공개 여부</li>
 *     <li>title: 공지사항 제목</li>
 *     <li>body: 공지사항 내용</li>
 *     <li>writer: 작성자 이름</li>
 *     <li>writerdate: 작성일시</li>
 * </ul>
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NoticeDetailDTO {

    /** 공지사항 고유 ID */
    private Long id;

    /** 작성자(회원) FK */
    private Long memberIdFk;

    /** 공지사항 카테고리 */
    private NoticeCategory noticeCategory;

    /** 공지사항 우선순위 */
    private NoticePriority noticePriority;

    /** 공지사항 공개 여부 */
    private boolean isShow;

    /** 공지사항 제목 */
    private String title;

    /** 공지사항 내용 */
    private String body;

    /** 작성자 이름 */
    private String writer;

    /** 작성일시 */
    @Schema(type="string", format="date-time")
    private LocalDateTime registeredAt;
}
