package com.boot.ict05_final_admin.domain.member.dto;

import lombok.Data;

@Data
public class MemberSearchDTO {
    private String keyword;
    private String type;
    private String size = "10";
}
