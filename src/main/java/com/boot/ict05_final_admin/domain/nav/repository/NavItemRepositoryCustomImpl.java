package com.boot.ict05_final_admin.domain.nav.repository;

import com.boot.ict05_final_admin.domain.nav.dto.NavListDTO;
import com.boot.ict05_final_admin.domain.nav.dto.NavSearchDTO;
import com.boot.ict05_final_admin.domain.nav.entity.QNavItem;
import com.boot.ict05_final_admin.domain.staffresources.dto.StaffSearchDTO;
import com.boot.ict05_final_admin.domain.staffresources.entity.QStaffProfile;
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
public class NavItemRepositoryCustomImpl implements NavItemRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<NavListDTO> listNav(NavSearchDTO navSearchDTO, Pageable pageable) {
        QNavItem navItem = QNavItem.navItem;

        List<NavListDTO> content = queryFactory
                .select(Projections.fields(NavListDTO.class,
                        navItem.id,
                        navItem.navItemCode,
                        navItem.navItemName,
                        navItem.navItemPath,
                        navItem.navItemEnabled
                ))
                .from(navItem)
                .where(
                        eqTitleOrBody(navSearchDTO, navItem)
                )
                .orderBy(navItem.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(navItem.count())
                .from(navItem)
                .where(
                        eqTitleOrBody(navSearchDTO, navItem)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression eqTitleOrBody(NavSearchDTO navSearchDTO, QNavItem navItem) {
        if(navSearchDTO.getKeyword() == null) {
            return null;
        }
        String keyword = navSearchDTO.getKeyword();

        return navItem.navItemName.stringValue().containsIgnoreCase(keyword)
                .or(navItem.navItemCode.stringValue().containsIgnoreCase(keyword))
                .or(navItem.navItemPath.stringValue().containsIgnoreCase(keyword));
    }
}
