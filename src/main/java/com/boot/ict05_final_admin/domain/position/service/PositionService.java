package com.boot.ict05_final_admin.domain.position.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.member.service.MemberService;
import com.boot.ict05_final_admin.domain.position.repository.PositionRepository;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffProfile;
import com.boot.ict05_final_admin.domain.staffresources.service.StaffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 회원과 사원(StaffProfile)의 연동을 담당하는 서비스.
 *
 * 업무 시나리오
 * 1 회원 식별자로 Member 조회
 * 2 회원 이메일과 동일한 사원 존재 여부 확인
 * 3 사원 엔티티 로드 및 기존 연동 여부 검증
 * 4 문제가 없으면 사원에 회원을 연결하고 영속성 컨텍스트에 반영
 *
 * 예외 정책
 * - 멤버, 사원 미존재 시 404
 * - 이미 다른 멤버와 연동되어 있으면 409
 */
@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class PositionService {

    private final MemberService memberService;
    private final StaffService staffService;
    private final PositionRepository positionRepository;

    /**
     * 회원과 사원을 이메일 기준으로 연동한다.
     *
     * 전제
     * - 회원의 이메일과 동일한 이메일을 가진 사원이 존재해야 한다.
     * - 해당 사원이 이미 다른 회원과 연동되어 있지 않아야 한다.
     *
     * 동작
     * - 회원 조회 → 사원 식별자 조회 → 사원 조회 → 연동 가능성 검증 → 연동 시행
     *
     * @param memberId 연동할 회원의 식별자
     * @return 연동이 완료된 사원 엔티티(영속 상태)
     * @throws ResponseStatusException 멤버/사원 미존재 또는 연동 충돌 시 HTTP 예외 발생
     */
    public StaffProfile memberLinkStaff(Long memberId) {

        Member member = memberService.findById(memberId);
        if (member == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "멤버가 존재하지 않습니다");

        Long staffId = positionRepository.searchStaffEmail(member.getEmail());
        if (staffId == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "동일 이메일의 사원이 없습니다");

        StaffProfile staff = staffService.findById(staffId);
        if (staff == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사원 정보가 존재하지 않습니다");

        // 이미 다른 멤버와 연결된 경우
        if (staff.getMember() != null && !staff.getMember().getId().equals(memberId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "해당 사원은 이미 다른 멤버와 연결되어 있습니다");

        staff.changeMember(member); // 영속 상태라 트랜잭션 종료 시 flush
        return staff;
    }
}
