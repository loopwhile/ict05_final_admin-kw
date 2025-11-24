package com.boot.ict05_final_admin.domain.notice.repository;

import com.boot.ict05_final_admin.domain.notice.dto.NoticeSearchDTO;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import com.boot.ict05_final_admin.domain.notice.dto.NoticeListDTO;

import com.boot.ict05_final_admin.domain.notice.entity.QNotice;
import com.querydsl.core.types.Projections;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryImpl implements NoticeRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<NoticeListDTO> listNotice(NoticeSearchDTO noticeSearchDTO, Pageable pageable) {
        QNotice notice = QNotice.notice;

        // 데이터 목록 조회
        List<NoticeListDTO> content = queryFactory
                .select(Projections.fields(NoticeListDTO.class,
                        notice.id,
                        notice.memberIdFk,
                        notice.noticeCategory,
                        notice.noticePriority,
                        notice.noticeStatus,
                        notice.isShow,
                        notice.title,
                        notice.body,
                        notice.writer,
                        notice.noticeCount,
                        notice.registeredAt
                        )) // member.name 매핑
                .from(notice)
                .where(
                        eqTitleOrBody(noticeSearchDTO, notice)
                )
                .orderBy(notice.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 조회
        long total = queryFactory
                .select(notice.count())
                .from(notice)
                .where(
                        eqTitleOrBody(noticeSearchDTO, notice)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }


    private BooleanExpression eqTitleOrBody(NoticeSearchDTO noticeSearchDTO, QNotice notice) {
        if (noticeSearchDTO.getType() == null || noticeSearchDTO.getS() == null) {
            return null; // 조건 없음
        }

        String keyword = noticeSearchDTO.getS();

        switch (noticeSearchDTO.getType()) {
            case "title":
                return notice.title.containsIgnoreCase(keyword);
            case "content":
                return notice.body.containsIgnoreCase(keyword);
            case "all": // title + body 모두 검색
                return notice.title.containsIgnoreCase(keyword)
                        .or(notice.body.containsIgnoreCase(keyword));
            default:
                return null;
        }
    }


    @Override
    public long countNotice(NoticeSearchDTO noticeSearchDTO) {
        QNotice notice = QNotice.notice;

        long total = queryFactory
                .select(notice.count())
                .from(notice)
                .where(
                        eqTitleOrBody(noticeSearchDTO, notice)
                )
                .fetchOne();

        return total;
    }
}
