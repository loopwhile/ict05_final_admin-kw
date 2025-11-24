package com.boot.ict05_final_admin.domain.staffresources.repository;

import com.boot.ict05_final_admin.domain.staffresources.dto.StaffListDTO;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffSearchDTO;
import com.boot.ict05_final_admin.domain.staffresources.entity.QStaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffDepartment;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.querydsl.core.types.dsl.Expressions;
import static com.querydsl.core.types.dsl.Expressions.numberTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StaffRepositoryImpl implements StaffRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StaffListDTO> listStaff(StaffSearchDTO staffSearchDTO, Pageable pageable) {

        QStaffProfile staffProfile = QStaffProfile.staffProfile;

        // 데이터 목록 조회
        List<StaffListDTO> content = queryFactory
                .select(Projections.fields(StaffListDTO.class,
                        staffProfile.id,
                        staffProfile.staffName,
                        staffProfile.staffEmploymentType,
                        staffProfile.staffDepartment,
                        staffProfile.staffBirth,
                        staffProfile.staffStartDate,
                        staffProfile.staffEndDate
                ))
                .from(staffProfile)
                .where(
                        eqTitleOrBody(staffSearchDTO, staffProfile)
                )
                .orderBy(staffProfile.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        //전체 카운트 조회
        long total = queryFactory
                .select(staffProfile.count())
                .from(staffProfile)
                .where(
                        eqTitleOrBody(staffSearchDTO, staffProfile)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countStaff(StaffSearchDTO staffSearchDTO) {
        QStaffProfile staffProfile = QStaffProfile.staffProfile;

        long total = queryFactory
                .select(staffProfile.count())
                .from(staffProfile)
                .where(
                        eqTitleOrBody(staffSearchDTO, staffProfile)
                )
                .fetchOne();

        return total;
    }

    @Override
    public long countAll() {
        QStaffProfile staffProfile = QStaffProfile.staffProfile;
        Long count = queryFactory.select(staffProfile.count()).from(staffProfile).fetchOne();
        return count == null ? 0 : count;
    }

    @Override
    public long countActive() {
        QStaffProfile staffProfile = QStaffProfile.staffProfile;
        Long count = queryFactory.select(staffProfile.count())
                .from(staffProfile)
                .where(staffProfile.staffEndDate.isNull())   // 재직: 퇴사일 null
                .fetchOne();
        return count == null ? 0 : count;
    }

    @Override
    public long countByDepartment(StaffDepartment dept) {
        QStaffProfile staffProfile = QStaffProfile.staffProfile;
        Long count = queryFactory.select(staffProfile.count())
                .from(staffProfile)
                .where(staffProfile.staffDepartment.eq(dept))
                .fetchOne();
        return count == null ? 0 : count;

    }

    @Override
    public double avgTenureYears(LocalDateTime now) {
        QStaffProfile staffProfile = QStaffProfile.staffProfile;

        // TIMESTAMPDIFF(MONTH, start, COALESCE(end, :now))
        var months = numberTemplate(Integer.class,
                "TIMESTAMPDIFF(MONTH, {0}, COALESCE({1}, {2}))",
                staffProfile.staffStartDate, staffProfile.staffEndDate, Expressions.constant(now));

        Double avgMonths = queryFactory.select(months.avg())
                .from(staffProfile)
                .fetchOne();

        if (avgMonths == null) return 0d;
        return avgMonths / 12.0;
    }

    private BooleanExpression eqTitleOrBody(StaffSearchDTO staffSearchDTO, QStaffProfile staffProfile) {
        if(staffSearchDTO.getKeyword() == null) {
            return null;
        }
        String keyword = staffSearchDTO.getKeyword();

        return staffProfile.id.stringValue().containsIgnoreCase(keyword)
                .or(staffProfile.staffName.stringValue().containsIgnoreCase(keyword))
                .or(staffProfile.staffDepartment.stringValue().containsIgnoreCase(keyword));
    }
}
