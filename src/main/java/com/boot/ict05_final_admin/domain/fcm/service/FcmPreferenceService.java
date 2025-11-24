package com.boot.ict05_final_admin.domain.fcm.service;

import com.boot.ict05_final_admin.domain.fcm.entity.AppType;
import com.boot.ict05_final_admin.domain.fcm.entity.FcmPreference;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FCM 수신 선호(Preference) 관리 서비스.
 *
 * <p>HQ 사용자 기준으로 수신 카테고리 및 임박 기준일 설정을 업서트/조회한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Service
@RequiredArgsConstructor
public class FcmPreferenceService {

	private final FcmPreferenceRepository prefRepo;

	/**
	 * HQ 회원의 선호 설정을 업서트한다.
	 *
	 * @param memberId      HQ 회원 ID
	 * @param catNotice     공지 수신 여부(옵션)
	 * @param catStockLow   재고 부족 수신 여부(옵션)
	 * @param catExpireSoon 유통기한 임박 수신 여부(옵션)
	 * @param thresholdDays 임박 기준 일수(옵션)
	 * @return 저장된 선호 엔티티
	 */
	@Transactional
	public FcmPreference upsertForHqMember(Long memberId,
										   Boolean catNotice,
										   Boolean catStockLow,
										   Boolean catExpireSoon,
										   Integer thresholdDays) {

		FcmPreference row = prefRepo.findFirstByAppTypeAndMemberIdFk(AppType.HQ, memberId)
				.orElseGet(() -> FcmPreference.builder()
						.appType(AppType.HQ)
						.memberIdFk(memberId)
						.build());

		if (catNotice     != null) row.setCatNotice(catNotice);
		if (catStockLow   != null) row.setCatStockLow(catStockLow);
		if (catExpireSoon != null) row.setCatExpireSoon(catExpireSoon);
		if (thresholdDays != null) row.setThresholdDays(thresholdDays);

		return prefRepo.save(row);
	}

	/**
	 * HQ 회원의 선호 설정을 조회한다.
	 *
	 * @param memberId HQ 회원 ID
	 * @return 선호 엔티티(없으면 null)
	 */
	@Transactional(readOnly = true)
	public FcmPreference getForHqMember(Long memberId) {
		return prefRepo.findFirstByAppTypeAndMemberIdFk(AppType.HQ, memberId).orElse(null);
	}
}
