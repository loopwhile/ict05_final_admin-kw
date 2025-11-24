package com.boot.ict05_final_admin.domain.staffresources.dto;

import lombok.Data;

@Data
public class StaffSearchDTO {
    private String keyword;
    private String type;
    private String size = "10";
}
