package com.boot.ict05_final_admin.domain.nav.dto;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NavListDTO {

    /** 시스템 메뉴 시퀀스 */
    private Long id;

    /** 시스템 메뉴 코드 */
    private String navItemCode;

    /** 시스템 메뉴 명 */
    private String navItemName;

    /** 시스템 메뉴 경로 */
    private String navItemPath;

    /** 시스템 메뉴 활성화여부 */
    private boolean navItemEnabled;
}
