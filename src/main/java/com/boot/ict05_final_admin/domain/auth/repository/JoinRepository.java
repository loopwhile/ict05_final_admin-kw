package com.boot.ict05_final_admin.domain.auth.repository;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JoinRepository extends JpaRepository<Member, Long> {
    boolean existsByEmail(String email);
    Optional<Member> findByEmail(String email);
//    boolean existsByEmailIgnoreCase(String email);
}
