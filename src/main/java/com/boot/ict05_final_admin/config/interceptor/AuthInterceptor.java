// src/main/java/com/boot/ict05_final_admin/config/interceptor/AuthInterceptor.java
package com.boot.ict05_final_admin.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private boolean isPermitPath(HttpServletRequest req) {
        String ctx = req.getContextPath(); // "/admin"
        String uri = req.getRequestURI();  // "/admin/..."
        return uri.startsWith(ctx + "/login")
                || uri.startsWith(ctx + "/register")
                || uri.startsWith(ctx + "/css")
                || uri.startsWith(ctx + "/js")
                || uri.startsWith(ctx + "/images")
                || uri.equals(ctx + "/")        // 루트는 시큐리티가 /login으로 안내
                || uri.startsWith(ctx + "/api/auth"); // 회원가입/중복체크 등 공개 API
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        if (isPermitPath(req)) return true;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        if (loggedIn) return true;

        res.sendRedirect(req.getContextPath() + "/login?error");
        return false;
    }
}
