package com.boot.ict05_final_admin.domain.staffresources.dto;

import com.boot.ict05_final_admin.domain.staffresources.entity.StaffDepartment;
import com.boot.ict05_final_admin.domain.staffresources.entity.StaffEmploymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StaffListDTO {

    /** 사원 시퀀스 */
    private Long id;

    /** 사원 이름 */
    private String staffName;

    /** 사원 근무형태 (점주/직원/알바) */
    private StaffEmploymentType staffEmploymentType;

    /** 사원 부서 (관리팀, 판매팀) */
    private StaffDepartment staffDepartment;

    /** 사원 생년월일 */
    @Schema(type="string", format="date-time")
    private LocalDateTime staffBirth;

    /** 사원 입사일자 (혹은 매장 근무 시작일) */
    @Schema(type="string", format="date-time")
    private LocalDateTime staffStartDate;

    /** 사원 퇴사일자 */
    @Schema(type="string", format="date-time")
    private LocalDateTime staffEndDate;

    /**
     * 생년월일을 "yyyy.MM.dd" 형식의 문자열로 반환한다.
     * 생년월일이 null이면 빈 문자열을 반환한다.
     *
     * @return 형식화된 생년월일 문자열
     */
    public String getStaffBirth() {
        if (staffBirth == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return staffBirth.format(formatter);
    }

    /**
     * 입사일자를 "yyyy.MM.dd" 형식의 문자열로 반환한다.
     * 입사일자가 null이면 빈 문자열을 반환한다.
     *
     * @return 형식화된 입사일자 문자열
     */
    public String getStaffStartDate() {
        if (staffStartDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return staffStartDate.format(formatter);
    }

    /**
     * 퇴사일자를 "yyyy.MM.dd" 형식의 문자열로 반환한다.
     * 퇴사일자가 null이면 빈 문자열을 반환한다.
     *
     * @return 형식화된 퇴사일자 문자열
     */
    public String getStaffEndDate() {
        if (staffEndDate == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return staffEndDate.format(formatter);
    }

}
