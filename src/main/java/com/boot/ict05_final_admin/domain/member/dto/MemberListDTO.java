package com.boot.ict05_final_admin.domain.member.dto;

import com.boot.ict05_final_admin.domain.auth.entity.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberListDTO {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private MemberStatus status;
}
