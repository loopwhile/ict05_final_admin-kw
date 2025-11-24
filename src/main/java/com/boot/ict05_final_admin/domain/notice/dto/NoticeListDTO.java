package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import com.boot.ict05_final_admin.domain.notice.entity.NoticeStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 공지사항 목록 조회 DTO
 *
 * <p>공지사항 목록 조회 시 사용되는 데이터 전송 객체(DTO)이다.
 * 공지사항의 기본 정보와 작성일자를 포함하며, 작성일자는
 * 형식화된 문자열로 변환 가능하다.</p>
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
 *     <li>writerdate: 작성일시 (LocalDateTime)</li>
 * </ul>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoticeListDTO {

    /**
     * 공지사항 고유 ID
     */
    private Long id;

    /**
     * 작성자(회원) FK
     */
    private Long memberIdFk;

    /**
     * 공지사항 카테고리
     */
    private NoticeCategory noticeCategory;

    /**
     * 공지사항 우선순위
     */
    private NoticePriority noticePriority;

    /**
     * 공지사항 상태
     */
    private NoticeStatus noticeStatus;

    /**
     * 공지사항 공개 여부
     */
    private boolean isShow;

    /**
     * 공지사항 제목
     */
    private String title;

    /**
     * 공지사항 내용
     */
    private String body;

    /**
     * 작성자 이름
     */
    private String writer;

    /**
     * 조회수
     */
    private Integer noticeCount;

    /**
     * 작성일시
     */
    @Schema(type = "string", format = "date-time")
    private LocalDateTime registeredAt;

    /**
     * 작성일자를 "yyyy.MM.dd" 형식의 문자열로 반환한다.
     * 작성일자가 null이면 빈 문자열을 반환한다.
     *
     * @return 형식화된 작성일자 문자열
     */
    public String getWriteDate() {
        if (registeredAt == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return registeredAt.format(formatter);
    }

    /** 한글 라벨: 상태 */
    public String getNoticeStatusLabel() {
        return noticeStatus != null ? noticeStatus.getDescription() : "";
    }

    /** 한글 라벨: 카테고리 */
    public String getNoticeCategoryLabel() {
        return noticeCategory != null ? noticeCategory.getDescription() : "";
    }

    /** 한글 라벨: 우선순위 */
    public String getNoticePriorityLabel() {
        return noticePriority != null ? noticePriority.getDescription() : "";
    }
}