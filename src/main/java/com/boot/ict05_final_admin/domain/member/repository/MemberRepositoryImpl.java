package com.boot.ict05_final_admin.domain.member.repository;

import com.boot.ict05_final_admin.domain.auth.entity.QMember;
import com.boot.ict05_final_admin.domain.member.dto.MemberListDTO;
import com.boot.ict05_final_admin.domain.member.dto.MemberSearchDTO;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MemberListDTO> listMember(MemberSearchDTO memberSearchDTO, Pageable pageable) {
        QMember member = QMember.member;

        List<MemberListDTO> content = queryFactory
                .select(Projections.fields(MemberListDTO.class,
                        member.id,
                        member.name,
                        member.email,
                        member.phone,
                        member.status.as("status")
                ))
                .from(member)
                .where(
                    eqTitleOrBody(memberSearchDTO, member)
                )
                .orderBy(member.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(member.count())
                .from(member)
                .where(
                        eqTitleOrBody(memberSearchDTO, member)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression eqTitleOrBody(MemberSearchDTO memberSearchDTO, QMember member) {
        if(memberSearchDTO.getKeyword() == null) {
            return null;
        }
        String keyword = memberSearchDTO.getKeyword();

        return member.id.stringValue().containsIgnoreCase(keyword)
                .or(member.name.stringValue().containsIgnoreCase(keyword))
                .or(member.id.stringValue().containsIgnoreCase(keyword))
                .or(member.email.stringValue().containsIgnoreCase(keyword));
    }
}
