package com.boot.ict05_final_admin.domain.member.dto;

import com.boot.ict05_final_admin.domain.auth.entity.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberModifyFormDTO {

    private Long id;

    private String memberName;

    private String memberEmail;

    private String memberPhone;

    private MemberStatus memberStatus;
}
