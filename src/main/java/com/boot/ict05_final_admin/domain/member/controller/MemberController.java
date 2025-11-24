package com.boot.ict05_final_admin.domain.member.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.auth.entity.MemberStatus;
import com.boot.ict05_final_admin.domain.member.dto.MemberListDTO;
import com.boot.ict05_final_admin.domain.member.dto.MemberSearchDTO;
import com.boot.ict05_final_admin.domain.member.service.MemberService;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 회원 관리 화면 컨트롤러.
 *
 * 회원 목록, 상세 조회, 수정 화면, 탈퇴 처리 등의 화면 렌더링을 담당한다.
 * 서비스 계층(MemberService)에서 데이터를 조회하거나 상태를 변경하고,
 * 뷰 렌더링에 필요한 모델을 구성해 반환한다.
 */
@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MyPageService myPageService;

    /**
     * 회원 목록을 페이징으로 조회해 목록 화면을 그린다.
     *
     * 화면에서 1페이지를 전달해도 내부 PageRequest는 0부터 시작하므로 1을 빼서 생성한다.
     *
     * @param memberSearchDTO 검색 조건(이름, 상태 등 선택 입력)
     * @param pageable 페이지 번호, 크기, 정렬 정보를 담은 객체(기본 page=1, size=10, id 내림차순)
     * @param model 뷰에 전달할 모델
     * @param request 현재 요청 정보(필터·페이징 유지용 URL 빌더에 사용)
     * @return 회원 목록 뷰 이름
     */
    @GetMapping("/member/list")
    public String ListOfficeMember(MemberSearchDTO memberSearchDTO,
                                   @PageableDefault(page = 1, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                   Model model,
                                   HttpServletRequest request) {
        PageRequest pageRequest = PageRequest.of(
                pageable.getPageNumber()-1,
                pageable.getPageSize(),
                Sort.by("id").descending());

        Page<MemberListDTO> members = memberService.selectAllMember(memberSearchDTO, pageRequest);

        model.addAttribute("members", members);
        model.addAttribute("urlBuilder", ServletUriComponentsBuilder.fromRequest(request));
        model.addAttribute("memberSearchDTO", memberSearchDTO);

        return "member/list";
    }

    /**
     * 회원 상세 정보를 조회해 상세 화면을 그린다.
     *
     * @param id 회원 식별자
     * @param model 뷰에 전달할 모델(회원 정보)
     * @return 회원 상세 뷰 이름
     */
    @GetMapping("/member/detail/{id}")
    public String detailOfficeMember(@PathVariable Long id, Model model) {
        Member member = memberService.detailMember(id);

        model.addAttribute("member", member);

        return "member/detail";
    }

    /**
     * 회원 수정 화면을 표시한다.
     *
     * 상세 정보를 불러와 폼에 채우고, 상태 선택을 위해 MemberStatus 값을 전달한다.
     *
     * @param id 회원 식별자
     * @param model 뷰에 전달할 모델(회원 정보, 상태 선택값)
     * @return 회원 수정 뷰 이름
     */
    @GetMapping("/member/modify/{id}")
    public String modifyOfficeMember(@PathVariable Long id, Model model) {

        Member member = memberService.detailMember(id);

        model.addAttribute("member", member);
        model.addAttribute("MemberStatus", MemberStatus.values());

        return "member/modify";
    }

    /**
     * 회원 탈퇴 처리를 수행하고 목록으로 리다이렉트한다.
     *
     * 화면에서는 GET으로 호출하지만, 실제 상태 변경을 수행하므로
     * 운영 시에는 POST 또는 PATCH로 전환하는 것을 권장한다.
     *
     * @param id 회원 식별자
     * @param model 뷰에 전달할 모델(사용하지 않음)
     * @return 회원 목록으로 리다이렉트
     */
    @GetMapping("/member/withdraw/{id}")
    public String withdrawOfficeMember(@PathVariable Long id, Model model) {
        myPageService.withdrawMember(id);
        return "redirect:/member/list";
    }
}
