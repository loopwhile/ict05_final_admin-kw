// src/main/java/com/boot/ict05_final_admin/domain/auth/service/MemberService.java
package com.boot.ict05_final_admin.domain.auth.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.auth.repository.JoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JoinService {
    private final JoinRepository joinRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long register(String email, String rawPassword, String name, String phone) {
        if (joinRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        Member m = new Member();
        m.setEmail(email);
        m.setPassword(passwordEncoder.encode(rawPassword)); // BCrypt 적용
        m.setName(name);
        m.setPhone(phone);
        return joinRepository.save(m).getId();
    }
}
