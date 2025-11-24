package com.boot.ict05_final_admin.domain.member.repository;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
}
