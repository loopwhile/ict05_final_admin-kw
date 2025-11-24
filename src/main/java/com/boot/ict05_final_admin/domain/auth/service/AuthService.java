package com.boot.ict05_final_admin.domain.auth.service;

import com.boot.ict05_final_admin.domain.auth.dto.JoinRequest;
import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.auth.entity.MemberStatus;
import com.boot.ict05_final_admin.domain.auth.repository.JoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final JoinRepository joinRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long join(JoinRequest req){
        if (joinRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }
        Member m = Member.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .status(MemberStatus.ACTIVE)
                .build();
        return joinRepository.save(m).getId();
    }

    @Transactional(readOnly = true)
    public boolean emailExists(String email){
        return joinRepository.existsByEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member m = joinRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));

        return org.springframework.security.core.userdetails.User
                .withUsername(m.getEmail())
                .password(m.getPassword())   // DB에 인코딩된 비번이 들어 있어야 함
                .roles("ADMIN")              // 혹은 m.getRole().name() (ROLE_ 접두어 자동)
                .build();
    }
}
