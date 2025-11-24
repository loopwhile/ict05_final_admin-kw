package com.boot.ict05_final_admin.domain.nav.controller;

import com.boot.ict05_final_admin.domain.nav.dto.NavListDTO;
import com.boot.ict05_final_admin.domain.nav.dto.NavSearchDTO;
import com.boot.ict05_final_admin.domain.nav.service.NavGateService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 시스템 메뉴 관리 화면 컨트롤러
 *
 * 역할
 * - 시스템 메뉴 목록 화면 렌더링
 * - 검색 조건 바인딩과 페이징 처리
 * - 템플릿이 사용할 모델 속성 구성
 *
 * 보안
 * - 화면 접근 제어는 Security 설정 또는 컨트롤러 단 @PreAuthorize 에서 처리 가능
 */
@Controller
@RequiredArgsConstructor
public class NavauthController {

    private final NavGateService navGateService;

    /**
     * 메뉴 권한 목록 화면
     *
     * 경로
     * - GET /navauth/list
     *
     * 요청 파라미터
     * - NavSearchDTO: 검색어, 필터 등의 폼 입력이 자동 바인딩된다
     * - Pageable: 화면 페이징 정보. @PageableDefault 로 기본값을 지정한다
     *   page는 1부터 시작하도록 UI를 설계했기 때문에 내부에서 0 기반으로 보정한다
     *
     * 모델 속성
     * - navs: Page<NavListDTO>. 목록과 페이징 메타데이터를 포함한다
     * - urlBuilder: 현재 요청을 기준으로 한 URL 빌더. 템플릿에서 페이징 링크 생성에 사용한다
     * - navSearchDTO: 검색 폼 값 유지용
     *
     * 뷰
     * - navauth/list
     *
     * 참고
     * - 리버스 프록시 뒤에서 올바른 절대 URL을 만들려면 Forwarded 헤더를 신뢰하도록 설정해야 한다
     *   예) @Bean ForwardedHeaderFilter 또는 server.forward-headers-strategy 사용
     */
    @GetMapping("/navauth/list")
    public String listOfficeNavauth(NavSearchDTO navSearchDTO,
                                    @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                    Model model,
                                    HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(
                // UI는 page를 1부터 전달하므로 JPA PageRequest(0 기반)로 변환한다
                pageable.getPageNumber()-1,
                pageable.getPageSize(),
                Sort.by("id").descending());

        // 검색 조건과 페이징으로 목록 조회
        Page<NavListDTO> navs = navGateService.selectAllNav(navSearchDTO, pageRequest);

        // 뷰에서 사용할 모델 구성
        model.addAttribute("navs", navs);
        // 현재 요청 기반으로 쿼리 파라미터를 유지한 링크를 만들 수 있도록 빌더를 전달한다
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("navSearchDTO", navSearchDTO);

        // 타임리프 템플릿 반환
        return "navauth/list";
    }
}
