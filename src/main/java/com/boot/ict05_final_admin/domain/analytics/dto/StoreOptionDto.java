package com.boot.ict05_final_admin.domain.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 가맹점(점포) 선택 옵션 DTO.
 *
 * <p>UI 셀렉트 박스 등에 표시할 ID/Name 페어를 제공한다.</p>
 */
@Getter
@AllArgsConstructor
public class StoreOptionDto {

	/** 점포 ID */
	private Long id;

	/** 점포명 */
	private String name;
}
