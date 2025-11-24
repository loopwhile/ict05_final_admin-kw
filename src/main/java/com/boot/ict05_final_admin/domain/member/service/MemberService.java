package com.boot.ict05_final_admin.domain.member.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.member.dto.MemberListDTO;
import com.boot.ict05_final_admin.domain.member.dto.MemberModifyFormDTO;
import com.boot.ict05_final_admin.domain.member.dto.MemberSearchDTO;
import com.boot.ict05_final_admin.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원 목록 페이지 조회.
     *
     * 설명
     * - 검색 조건과 페이징 정보를 받아 커스텀 쿼리로 Page<MemberListDTO>를 반환한다.
     * - 검색 조건은 MemberSearchDTO에 캡슐화되어 있으며, 레포지토리에서 동적 조건으로 해석한다.
     */
    public Page<MemberListDTO> selectAllMember(MemberSearchDTO memberSearchDTO, Pageable pageable) {
        return memberRepository.listMember(memberSearchDTO, pageable);
    }

    /**
     * 단일 회원 상세 조회(뷰용).
     *
     * 반환
     * - 회원이 없으면 null 반환. 컨트롤러에서 null 대응(404 또는 안내 메시지) 처리 권장.
     *
     * 참고
     * - 본 메서드는 클래스 레벨 @Transactional 의 영향을 받으며 기본적으로 쓰기 가능 트랜잭션 컨텍스트다.
     *   단순 조회만 수행하므로 읽기 전용이 필요하면 별도 readOnly 메서드를 사용하는 것도 가능하다.
     */
    public Member detailMember(Long id) { return memberRepository.findById(id).orElse(null);}

    /**
     * 단일 회원 조회(readOnly).
     *
     * 목적
     * - 외부에서 순수 조회 용도로 사용할 때를 대비해 readOnly 힌트를 부여한다.
     *
     * 주의
     * - 같은 클래스 내부에서 이 메서드를 호출(memberModify → findById)하는 경우
     *   프록시를 경유하지 않는 자기 호출이라 readOnly 속성이 적용되지 않는다.
     *   즉, memberModify 트랜잭션의 속성이 우선한다.
     */
    @Transactional(readOnly = true)
    public Member findById(Long id) { return memberRepository.findById(id).orElse(null);}

    /**
     * 회원 수정.
     *
     * 흐름
     * 1. 식별자로 영속 엔티티 로드(없으면 IllegalArgumentException).
     * 2. 엔티티의 updateMember(dto) 호출로 필드 변경.
     * 3. 트랜잭션 커밋 시 JPA 더티체킹으로 변경사항 flush.
     *
     * 예외
     * - 대상 회원이 없으면 IllegalArgumentException 발생.
     *
     * 비고
     * - 비밀번호와 같은 민감 정보가 DTO에 포함될 경우 별도 인코딩 처리와 변경 정책 검증이 필요하다.
     *   현재는 Member.updateMember(dto) 내부 정책에 위임한다.
     */
    public Member memberModify(MemberModifyFormDTO dto) {

        Member member = findById(dto.getId());
        if (member == null) throw new IllegalArgumentException("해당 회원이 존재하지 않습니다.");

        member.updateMember(dto);

        return member;
    }


}
