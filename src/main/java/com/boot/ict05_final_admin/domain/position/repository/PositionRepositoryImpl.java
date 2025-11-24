package com.boot.ict05_final_admin.domain.position.repository;

import com.boot.ict05_final_admin.domain.auth.entity.QMember;
import com.boot.ict05_final_admin.domain.staffresources.entity.QStaffProfile;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PositionRepositoryImpl implements PositionRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Long searchStaffEmail(String email) {
        QMember member = QMember.member;
        QStaffProfile staffProfile = QStaffProfile.staffProfile;

        return queryFactory
                .select(staffProfile.id)
                .from(staffProfile)
                .where(
                        staffProfile.staffEmail.eq(email)
                )
                .fetchOne();
    }
}
