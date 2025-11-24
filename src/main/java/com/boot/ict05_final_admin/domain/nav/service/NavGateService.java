package com.boot.ict05_final_admin.domain.nav.service;

import com.boot.ict05_final_admin.domain.nav.dto.NavListDTO;
import com.boot.ict05_final_admin.domain.nav.dto.NavSearchDTO;
import com.boot.ict05_final_admin.domain.nav.entity.NavItem;
import com.boot.ict05_final_admin.domain.nav.repository.NavItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * 시스템 내 네비게이션 항목 접근 제어 게이트 서비스
 *
 * 역할
 * 1 요청 URI가 현재 네비게이션 정책상 활성 상태인지 판정
 * 2 시스템 메뉴 목록 조회 (검색·페이징)
 * 3 단일 메뉴의 활성화 상태 변경 또는 토글
 *
 * 트랜잭션
 * - 클래스 기본은 읽기 전용
 * - 상태 변경 메소드는 개별적으로 @Transactional 로 격리
 *
 * 보안
 * - 여기서 true/false는 네비게이션 관점의 허용 여부
 * - 최종 접근 통제는 Spring Security에서 담당
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NavGateService {

    private final NavItemRepository navItemRepository;

    /**
     * 요청 URI가 nav 정책상 활성인지 판정한다.
     *
     * 동작
     * - 모든 NavItem을 로드한 뒤 요청 URI로 시작하는 항목들 중
     *   경로가 가장 긴 항목을 선택한다 (가장 구체적인 정책 우선).
     * - 매칭되는 항목이 없으면 nav에서 관리하지 않는 경로로 간주하고 통과(true)시킨다.
     *   최종 보안은 Security가 수행한다.
     *
     * 예
     * - /admin/API 와 /admin/API/staff 가 모두 등록된 경우
     *   /admin/API/staff/modify 요청은 후자를 우선한다.
     */
    public boolean isEnabledPath(String requestUri) {
        List<NavItem> all = navItemRepository.findAll();

        // 가장 긴 prefix를 가진 항목 우선
        Optional<NavItem> match = all.stream()
                .filter(n -> requestUri.startsWith(n.getNavItemPath()))
                .max(Comparator.comparingInt(n -> n.getNavItemPath().length()));

        // nav에서 관리하지 않는 경로면 true 반환
        // 최종 차단은 Security가 담당
        return match.map(NavItem::isNavItemEnabled).orElse(true);
    }

    /**
     * 시스템 메뉴 목록을 검색어로 필터링하여 페이지 단위로 조회한다.
     *
     * @param navSearchDTO 검색 조건 (선택)
     * @param pageable 페이지 정보
     * @return 페이지 결과 DTO
     */
    public Page<NavListDTO> selectAllNav(NavSearchDTO navSearchDTO, Pageable pageable) {
        return navItemRepository.listNav(navSearchDTO, pageable);
    }

    /**
     * 단일 메뉴의 활성화 상태를 설정한다.
     *
     * 구현
     * - update 쿼리로 직접 갱신한다.
     * - 갱신 행 수가 0이면 대상 없음으로 판단하여 예외를 던진다.
     *
     * @param id 대상 메뉴 식별자
     * @param enabled 설정할 상태
     * @return 설정된 상태 값
     */
    @Transactional
    public boolean setEnabled(Long id, boolean enabled) {
        int cnt = navItemRepository.updateEnabled(id, enabled);
        if (cnt == 0) throw new IllegalArgumentException("대상 메뉴가 없습니다: " + id);
        return enabled;
    }

    /**
     * 단일 메뉴의 활성화 상태를 반전한다.
     *
     * 구현
     * - 엔티티를 조회하여 enabled 값을 반전하고 더티 체킹으로 저장한다.
     * - 토글 특성상 멱등이 아니다.
     *
     * @param id 대상 메뉴 식별자
     * @return 토글 후의 상태 값
     */
    @Transactional
    public boolean toggle(Long id) {
        NavItem nav = navItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("대상 메뉴가 없습니다: " + id));
        boolean next = !nav.isNavItemEnabled();
        nav.setNavItemEnabled(next); // dirty checking
        return next;
    }
}
