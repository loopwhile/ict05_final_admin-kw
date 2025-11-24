package com.boot.ict05_final_admin.domain.fcm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * HQ 사용자용 FCM 수신 설정 화면을 제공하는 뷰 컨트롤러.
 *
 * <p>Thymeleaf 템플릿을 렌더링하여 사용자에게 알림 설정 UI를 제공한다.</p>
 *
 * @author 이경욱
 * @since 2025-11-10
 */
@Controller
@RequestMapping("/fcm/pref")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('HQ','ADMIN')")
public class HqFcmViewController {

    /**
     * FCM 수신 설정 페이지를 반환한다.
     *
     * @return "fcm/preference" 뷰 이름
     */
    @GetMapping
    public String getFcmPreferencePage() {
        return "fcm/preference";
    }
}
