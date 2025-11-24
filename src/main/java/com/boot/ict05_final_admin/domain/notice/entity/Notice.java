package com.boot.ict05_final_admin.domain.notice.entity;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeModifyFormDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공지사항(Notice) 엔티티 클래스
 *
 * <p>본 클래스는 공지사항 테이블과 매핑되며,
 * 공지사항의 카테고리, 우선순위, 제목, 내용, 작성자, 작성일 등의 정보를 포함합니다.</p>
 *
 * <p>엔티티는 생성, 조회, 수정 기능을 지원하며,
 * {@link #updateNotice(NoticeModifyFormDTO)} 메서드를 통해 상태를 변경할 수 있습니다.</p>
 *
 * @author 김동관
 * @since 2025-09-30
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    /** 공지사항 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long id;

    /** 작성자(회원) FK */
    @Column(name = "member_id_fk")
    private Long memberIdFk;

    /** 공지사항 카테고리 */
    @Enumerated(EnumType.STRING)
    @Column(name = "notice_category")
    private NoticeCategory noticeCategory;

    /** 공지사항 우선순위 */
    @Enumerated(EnumType.STRING)
    @Column(name = "notice_priority")
    private NoticePriority noticePriority;

    /** 공지사항 상태  */
    @Enumerated(EnumType.STRING)
    @Column(name = "notice_status")
    private NoticeStatus noticeStatus;

    /** 공지사항 노출 여부 */
    @Column(name = "is_show")
    private Boolean isShow;

    /** 작성자 이름 */
    @Column(name = "writer")
    private String writer;

    /** 공지사항 제목 */
    @Column(name = "notice_title")
    private String title;

    /** 공지사항 본문 내용 */
    @Column(name = "notice_content")
    private String body;

    /** 작성일자 */
    @Schema(type="string", format="date-time")
    @Column(name = "notice_reg_date")
    private LocalDateTime registeredAt;

    /** 조회수 */
    @Column(name = "notice_count")
    private Integer noticeCount;

    /** 확인 여부 (TINYINT(1) → Boolean) */
    @Column(name = "notice_confirmed")
    private Boolean noticeConfirmed;

    /**
     * 공지사항 정보를 수정하는 메서드
     *
     * <p>입력된 {@link NoticeModifyFormDTO} 객체의 데이터를 기준으로
     * 공지사항 엔티티의 상태를 변경합니다. 수정 시 작성일자는 현재 시간으로 갱신됩니다.</p>
     *
     * @param dto 수정할 공지사항 정보를 담고 있는 DTO 객체
     */
    public void updateNotice(NoticeModifyFormDTO dto) {
        this.noticeCategory   = dto.getNoticeCategory();
        this.noticePriority   = dto.getNoticePriority();
        this.isShow           = dto.getIsShow();
        this.title            = dto.getTitle();
        this.body             = dto.getBody();
        this.writer           = dto.getWriter();
        this.registeredAt     = LocalDateTime.now();
    }
}
