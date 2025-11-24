package com.boot.ict05_final_admin.domain.myPage.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.myPage.dto.MyPageDTO;
import com.boot.ict05_final_admin.domain.myPage.repository.MyPageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 마이페이지 관련 비즈니스 로직 서비스 클래스.
 *
 * <p>로그인된 회원의 정보를 기반으로 마이페이지 조회, 수정, 비밀번호 변경, 탈퇴(Soft Delete)를 수행한다.<br>
 * 모든 변경은 로그인된 사용자의 ID를 SecurityContext에서 추출하여 처리한다.</p>
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class MyPageService {

    private final MyPageRepository myPageRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 마이페이지 조회
     *
     * <p>로그인된 회원 ID를 기반으로 회원 정보를 조회하고,
     * {@link MyPageDTO}로 변환하여 반환한다.</p>
     *
     * @param memberId 현재 로그인된 회원의 ID
     * @return 회원 정보 DTO
     * @throws IllegalArgumentException 회원이 존재하지 않을 경우 발생
     */
    @Transactional(readOnly = true)
    public MyPageDTO getMyPage(Long memberId) {
        Member member = myPageRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        return MyPageDTO.fromEntity(member);
    }

    /**
     * 이메일로 회원 조회
     *
     * @param email 회원 이메일
     * @return 해당 이메일의 {@link Member} 엔티티
     * @throws IllegalArgumentException 회원이 존재하지 않을 경우 발생
     */
    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return myPageRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 회원이 존재하지 않습니다."));
    }

    /**
     * 회원 기본 정보 수정
     *
     * <p>이름과 전화번호를 수정한다. 로그인된 회원 본인만 수정할 수 있다.</p>
     *
     * @param dto 수정할 회원 정보 DTO
     * @throws IllegalArgumentException 회원이 존재하지 않을 경우 발생
     */
    @Transactional
    public void updateMember(Long memberId, MyPageDTO dto) {
        Member member = myPageRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 기본 정보
        member.updateProfile(dto.getName(), dto.getPhone());

        // 프로필 이미지 경로 반영(있을 때만)
        if (dto.getMemberImagePath() != null && !dto.getMemberImagePath().isBlank()) {
            member.setMemberImagePath(dto.getMemberImagePath());
        } else {
            // 이미지가 새로 업로드되지 않았을 때도 dirty-check 유도
            // 무조건 flush 되게 강제 저장
            member.setMemberImagePath(member.getMemberImagePath());
        }
        // 강제로 저장 및 flush (강제 DB 반영 (dirty-check 무시)
        myPageRepository.saveAndFlush(member);
    }

    /**
     * 비밀번호 변경
     *
     * <p>현재 비밀번호를 검증한 후 새 비밀번호를 암호화하여 저장한다.<br>
     *
     * @param memberId 로그인된 회원 ID
     * @param currentPassword 입력한 현재 비밀번호
     * @param newPassword 변경할 새 비밀번호
     * @throws IllegalArgumentException 현재 비밀번호 불일치 또는 회원 미존재 시 발생
     */
    //Spring Security의 {@link PasswordEncoder}를 사용한다.</p>
    @Transactional
    public void updatePassword(Long memberId, String currentPassword, String newPassword) {
        Member member = myPageRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        member.setPassword(passwordEncoder.encode(newPassword));
    }

    /**
     * 비밀번호 검증 (AJAX 요청용)
     *
     * <p>입력된 현재 비밀번호가 실제 회원 비밀번호와 일치하는지 확인한다.</p>
     *
     * @param memberId 로그인된 회원 ID
     * @param currentPassword 입력된 비밀번호
     * @return 일치 여부 (true = 일치, false = 불일치)
     * @throws IllegalArgumentException 회원이 존재하지 않을 경우 발생
     */
    @Transactional(readOnly = true)
    public boolean checkCurrentPassword(Long memberId, String currentPassword) {
        Member member = myPageRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        return passwordEncoder.matches(currentPassword, member.getPassword());
    }

    /**
     * 회원 탈퇴 처리 (Soft Delete)
     *
     * <p>회원 상태를 {@code WITHDRAWN}으로 변경한다.<br>
     * 실제 삭제는 하지 않고, 비활성 상태로 전환한다.</p>
     *
     * @param memberId 로그인된 회원 ID
     * @throws IllegalArgumentException 회원이 존재하지 않을 경우 발생
     */
    public void withdrawMember(Long memberId) {
        Member member = myPageRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        member.withdraw(); // 상태를 WITHDRAWN으로 변경
    }

}
