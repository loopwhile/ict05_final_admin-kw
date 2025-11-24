package com.boot.ict05_final_admin.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 프로젝트 공통 속성
 */
//@ControllerAdvice
@Component
public class ProjectAttribute {

    /**
     * 타임리프 URI 접근용
     *
     * @author SungHa
     */
    @ModelAttribute("requestValue")
    public HttpServletRequest getServletRequest(HttpServletRequest request) {
        return request;
    }

    /**
     * 타임리프 페이징
     *
     * @author SungHa
     */
    @ModelAttribute("urlBuilder")
    public ServletUriComponentsBuilder getServletUriComponentsBuilder(ServletUriComponentsBuilder uriComponentsBuilder) {
        return uriComponentsBuilder;
    }
}