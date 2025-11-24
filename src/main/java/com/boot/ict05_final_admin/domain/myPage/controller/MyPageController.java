package com.boot.ict05_final_admin.domain.myPage.controller;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.myPage.dto.MyPageDTO;
import com.boot.ict05_final_admin.domain.myPage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 관리자 마이페이지 관련 컨트롤러
 * <p>
 * 마이페이지 상세 조회, 수정 화면을 제공한다.
 * 비밀번호 변경, 탈퇴 기능을 지원한다.
 */
@Controller
@RequiredArgsConstructor
@Tag(name = "마이페이지", description = "회원 프로필 조회 및 수정 API")
public class MyPageController {

    private final MyPageService myPageService;
    private Member member;

    @Value("${file.upload-dir.profile}")
    private String profileUploadDir;

    /**
     * 마이페이지 조회
     * - SecurityContext에서 memberId 자동 추출
     */
    @GetMapping("/mypage")
    @Operation(summary = "마이페이지 상세 조회", description = "회원의 프로필 정보를 조회한다.")
    public String myPage(Model model) {

        Long memberId = getLoginMemberId();;

        // 마이페이지 조회
        MyPageDTO dto = myPageService.getMyPage(memberId);
        model.addAttribute("member", dto);
        return "myPage/view";
    }

    /**
     * 현재 로그인한 사용자의 회원 ID를 반환한다.
     *
     * <p>Spring Security의 {@link SecurityContextHolder}에서 인증(Authentication) 정보를 가져와
     * 로그인된 사용자의 principal 객체를 확인한다. principal은 보통 {@link org.springframework.security.core.userdetails.UserDetails}
     * 구현체이거나 {@link com.boot.ict05_final_admin.domain.auth.entity.Member} 엔티티일 수 있다.</p>
     *
     * <ul>
     *   <li>principal이 {@code Member} 타입이면 해당 엔티티의 ID를 바로 반환한다.</li>
     *   <li>principal이 {@code UserDetails} 타입이면 username(email)을 이용해 DB에서 회원을 조회한 후 ID를 반환한다.</li>
     * </ul>
     *
     * @return 로그인된 회원의 ID
     * @throws IllegalStateException 로그인되지 않았거나 인증 정보를 가져올 수 없는 경우
     */
    private Long getLoginMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("로그인되지 않은 사용자입니다.");
        }

        Object principal = authentication.getPrincipal();

        // principal 이 커스텀 Member 객체라면
        if (principal instanceof com.boot.ict05_final_admin.domain.auth.entity.Member member) {
            return member.getId();
        }

        // principal 이 UserDetails 타입이라면
        if (principal instanceof UserDetails userDetails) {
            // email(username)로 회원 조회
            com.boot.ict05_final_admin.domain.auth.entity.Member memberEntity =
                    myPageService.findByEmail(userDetails.getUsername());
            return memberEntity.getId();
        }

        throw new IllegalStateException("사용자 정보를 가져올 수 없습니다.");
    }

    /**
     * 회원 수정 폼 페이지
     *
     * @param model Thymeleaf에 전달할 모델
     * @return 수정 페이지 뷰
     */
    @GetMapping("/mypage/modify")
    @Operation(summary = "마이페이지 수정 폼", description = "기존 회원 데이터를 불러와 수정 입력 폼을 표시한다.")
    public String modifyForm(Model model) {

        Long memberId = getLoginMemberId();

        MyPageDTO dto = myPageService.getMyPage(memberId);
        model.addAttribute("member", dto);
        return "myPage/modify";
    }

    /**
     * 회원 정보 수정 처리
     *
     * @param member            수정할 회원 정보 DTO
     * @param memberImage   업로드할 새 프로필 이미지
     * @param currentPassword 현재 비밀번호
     * @param newPassword     새 비밀번호
     * @param confirmPassword 새 비밀번호 확인
     * @return 수정 후 마이페이지 상세 페이지로 리다이렉트
     */
    @PostMapping("/mypage/modify")
    @Operation(summary = "마이페이지 수정 처리", description = "이름, 전화번호, 비밀번호, 프로필 이미지를 한 번에 수정한다.")
    public String updateMember(@ModelAttribute("member") MyPageDTO member,
                               @RequestParam(value = "memberImage", required = false) MultipartFile memberImage,
                               @RequestParam(required = false) String currentPassword,
                               @RequestParam(required = false) String newPassword,
                               @RequestParam(required = false) String confirmPassword,
                               @RequestParam(value = "resetImage", required = false) String resetImage
                               ) throws IOException{

        Long memberId = getLoginMemberId();
        member.setId(memberId);

        // 기본 이미지 복원 요청 확인
        if ("true".equals(resetImage)) {
            member.setMemberImagePath(null);
        }

        // 프로필 이미지 업로드
        if (memberImage != null && !memberImage.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + memberImage.getOriginalFilename();

            Path path = Paths.get(profileUploadDir, fileName).toAbsolutePath().normalize();
            Files.createDirectories(path.getParent());
            memberImage.transferTo(path.toFile());

            // DB에는 파일 이름만
            member.setMemberImagePath(fileName);
        }

        // 이름, 전화번호, 이미지 경로 수정
        myPageService.updateMember(memberId, member);

        // 비밀번호 입력이 있는 경우만 처리
        if (currentPassword != null && !currentPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
            }
            myPageService.updatePassword(member.getId(), currentPassword, newPassword);
        }
        return "redirect:/mypage";
    }

    @ModelAttribute("member")
    public MyPageDTO defaultMember() {
        return new MyPageDTO(); // 필드는 null
    }

    /**
     * 비밀번호 검증 (AJAX)
     *
             * @param currentPassword 입력된 현재 비밀번호
     * @return 일치 여부 (true = 일치)
     */
    @PostMapping("/mypage/check-password")
    @ResponseBody
    @Operation(summary = "비밀번호 검증", description = "현재 비밀번호가 DB에 저장된 값과 일치하는지 확인한다.")
    public boolean checkCurrentPassword(@RequestParam String currentPassword) {

        Long memberId = getLoginMemberId();

        return myPageService.checkCurrentPassword(memberId, currentPassword);
    }

    /**
     * 회원 탈퇴 처리
     *
     * @param session 현재 세션
     * @return 로그인 페이지로 리다이렉트
     */
    @PostMapping("/mypage/withdraw")
    @Operation(summary = "회원 탈퇴", description = "회원 상태를 'WITHDRAWN'으로 변경하고 세션을 만료시킨다.")
    public String withdrawMember(HttpSession session) {

        Long memberId = getLoginMemberId();

        // 상태 변경 (WITHDRAWN)
        myPageService.withdrawMember(memberId);

        // 세션 만료 처리 (로그아웃 효과)
        session.invalidate();

        return "redirect:/login";
    }

}


