package com.boot.ict05_final_admin.domain.nav.dto;

import lombok.Data;

@Data
public class NavSearchDTO {
    private String keyword;
    private String type;
    private String size = "10";
}
