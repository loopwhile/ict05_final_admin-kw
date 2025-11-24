// src/main/java/com/boot/ict05_final_admin/domain/auth/service/MemberUserDetailsService.java
package com.boot.ict05_final_admin.domain.auth.service;

import com.boot.ict05_final_admin.domain.auth.entity.Member;
import com.boot.ict05_final_admin.domain.auth.repository.JoinRepository;
import com.boot.ict05_final_admin.domain.staffresources.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {
    private final JoinRepository joinRepository;
    private final StaffRepository staffRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member m = joinRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("no user: " + email));

        var authorities = new java.util.HashSet<org.springframework.security.core.authority.SimpleGrantedAuthority>();

        // 1) 기본값: StaffProfile 없으면 최소 본사 공통 권한을 주거나(선택) 로그인만 허용
        staffRepository.findByMember_Id(m.getId()).ifPresentOrElse(sp -> {
            // Department -> ROLE_* 매핑
            switch (sp.getStaffDepartment()) {
                case OFFICE     -> authorities.add(new SimpleGrantedAuthority("ROLE_HQ"));
                case STORE      -> authorities.add(new SimpleGrantedAuthority("ROLE_STORE"));
                case FRANCHISE  -> authorities.add(new SimpleGrantedAuthority("ROLE_FRANCHISE"));
                case OPS        -> authorities.add(new SimpleGrantedAuthority("ROLE_OPS"));
                case HR         -> authorities.add(new SimpleGrantedAuthority("ROLE_HR"));
                case ANALYTICS  -> authorities.add(new SimpleGrantedAuthority("ROLE_ANALYTICS"));
                case ADMIN      -> authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
        }, () -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_HQ"));
        });

        // DB 비번은 passwordEncoder.encode(...)로 저장되어 있어야 함.  {bcrypt}... 형태면 OK
        return User.withUsername(m.getEmail())
                .password(m.getPassword())
                .authorities(authorities) // 권한은 임시로 HQ
                .build();
    }
}
