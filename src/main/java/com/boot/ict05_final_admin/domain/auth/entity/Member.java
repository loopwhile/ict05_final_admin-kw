package com.boot.ict05_final_admin.domain.auth.entity;

//import com.boot.ict05_final_admin.domain.auth.UserRole;
import com.boot.ict05_final_admin.domain.member.dto.MemberModifyFormDTO;
import jakarta.persistence.*;
import lombok.*;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "member_email"),
                @UniqueConstraint(columnNames = "member_phone")
        }
)
public class Member  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_email", nullable = false, length = 254)
    private String email;

    @Column(name = "member_password", nullable = false, length = 255)
    private String password;

    @Column(name = "member_name", nullable = false, length = 100)
    private String name;

    @Column(name = "member_phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_status", nullable = false, length = 20)
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "member_image_path")
    private String memberImagePath;

//
//    // DB에 컬럼이 없으므로 우선 Transient (필요하면 테이블에 role 컬럼 추가)
//    @Transient
//    private UserRole role;

//    // --- UserDetails ---
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        // role이 없으므로 기본 ROLE_HQ 부여(본사 시스템)
//        return List.of((GrantedAuthority) () -> "ROLE_HQ");
//    }
//
//    @Override public String getUsername() { return email; }
//    @Override public String getPassword() { return password; }
//    @Override public boolean isAccountNonExpired() { return true; }
//    @Override public boolean isAccountNonLocked() { return true; }
//    @Override public boolean isCredentialsNonExpired() { return true; }
//    @Override public boolean isEnabled() { return true; }


    /**
     * 회원 정보 수정
     * - 마이페이지에서 이름, 전화번호 변경 시 사용
     */
    public void updateProfile(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    /**
     * 이미지 경로 수정
     * - 마이페이지에서 프로필 이미지 변경 시 사용
     */
    public void setMemberImagePath(String memberImagePath) {
        this.memberImagePath = memberImagePath;
    }

    /**
     * 비밀번호 수정
     * - 마이페이지에서 비밀번호 변경 시 사용
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 탈퇴 처리
     */
    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }

    public void updateMember(MemberModifyFormDTO dto) {
        this.name = dto.getMemberName();
        this.email = dto.getMemberEmail();
        this.phone = dto.getMemberPhone();
        this.status = dto.getMemberStatus();
    }

    @PrePersist
    void prePersist() {
        if (status == null) status = MemberStatus.ACTIVE;
    }
}
