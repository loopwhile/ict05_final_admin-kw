package com.boot.ict05_final_admin.domain.notice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공지사항 첨부파일 엔티티
 *
 * <p>공지사항에 첨부된 파일 정보를 저장한다.
 * 각 첨부파일은 고유 ID를 가지며, 해당 공지사항 ID와
 * 파일 URL을 포함한다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li>id: 첨부파일 고유 ID (자동 생성)</li>
 *     <li>noticeId: 첨부파일이 연결된 공지사항 ID</li>
 *     <li>url: 첨부파일 접근 URL</li>
 * </ul>
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class NoticeAttachment {

    /**
     * 첨부파일 고유 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 첨부파일이 연결된 공지사항 ID
     */
    private Long noticeId;

    /**
     * 첨부파일 URL
     */
    private String url;
    /**
     * 원본 파일명
     */
    private String originalFilename;
}
