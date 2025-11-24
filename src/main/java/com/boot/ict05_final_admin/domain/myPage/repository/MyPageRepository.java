package com.boot.ict05_final_admin.domain.myPage.repository;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyPageRepository extends JpaRepository<Member, Long>{

    /**
     * 이메일로 회원 조회
     *
     * @param email 회원 이메일
     * @return 회원 엔티티 Optional (존재하지 않을 수 있음)
     */
    Optional<Member> findByEmail(String email);

}
