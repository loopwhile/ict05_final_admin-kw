package com.boot.ict05_final_admin.domain.fcm.config;

import com.boot.ict05_final_admin.domain.fcm.entity.FcmTemplate;
import com.boot.ict05_final_admin.domain.fcm.repository.FcmTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FCM 템플릿 기본값을 멱등(upsert)으로 주입하는 초기화기.
 *
 * <p>개발환경에서 기본 템플릿을 자동 주입하도록 설정할 때 사용된다. 설정 프로퍼티
 * {@code fcm.seed.templates=true} 일 때 동작한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Component
@Order(20)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "fcm.seed.templates", havingValue = "true", matchIfMissing = true)
public class FcmTemplateDataInitializer implements ApplicationRunner {

	private final FcmTemplateRepository templateRepository;

	/**
	 * 애플리케이션 시작 시 템플릿을 멱등적으로 upsert 한다.
	 *
	 * @param args 애플리케이션 인수
	 */
	@Override
	@Transactional
	public void run(ApplicationArguments args) {

		upsert("HQ_STOCK_LOW",
				"[재고] 본사 재고 부족",
				"{materialName} 재고 {qty} (임계:{threshold})");

		upsert("HQ_EXPIRE_SOON",
				"[유통기한 임박] {materialName}",
				"{days}일 남음 (로트:{lot})");
	}

	/**
	 * 템플릿 코드를 기준으로 존재하면 업데이트, 없으면 생성하여 저장한다.
	 *
	 * @param code     템플릿 코드
	 * @param titleTpl 제목 템플릿
	 * @param bodyTpl  본문 템플릿
	 */
	private void upsert(String code, String titleTpl, String bodyTpl) {
		FcmTemplate row = templateRepository.findByTemplateCode(code)
				.orElseGet(() -> FcmTemplate.builder().templateCode(code).build());
		row.setTitleTemplate(titleTpl);
		row.setBodyTemplate(bodyTpl);
		templateRepository.save(row);
		log.info("[FCM] template upserted: {}", code);
	}
}
