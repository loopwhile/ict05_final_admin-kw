package com.boot.ict05_final_admin.domain.notice.dto;

import lombok.Data;

@Data
public class NoticeSearchDTO {
    private String s;
    private String type;
    private String size = "10";
}
