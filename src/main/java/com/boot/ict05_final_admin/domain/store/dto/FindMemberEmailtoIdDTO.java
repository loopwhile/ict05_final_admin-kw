package com.boot.ict05_final_admin.domain.store.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 회원 ID → 이메일 조회 DTO.
 *
 * <p>회원 또는 본사 담당자의 ID와 이메일을 조회할 때 사용되는
 * 간단한 데이터 전송 객체이다.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "회원 ID와 이메일 조회용 DTO")
public class FindMemberEmailtoIdDTO {

    /** 회원/직원 고유 ID */
    @Schema(description = "회원 또는 직원 ID", example = "101")
    private Long id;

    /** 이메일 */
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;
}
