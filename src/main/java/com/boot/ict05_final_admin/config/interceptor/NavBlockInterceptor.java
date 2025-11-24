package com.boot.ict05_final_admin.config.interceptor;

import com.boot.ict05_final_admin.domain.nav.service.NavGateService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class NavBlockInterceptor implements HandlerInterceptor {

    private final NavGateService navGateService;

    private boolean bypass(HttpServletRequest req) {
        String ctx = req.getContextPath();
        String uri = req.getRequestURI();
        return uri.startsWith(ctx + "/login")
                || uri.startsWith(ctx + "/register")
                || uri.startsWith(ctx + "/css")
                || uri.startsWith(ctx + "/js")
                || uri.startsWith(ctx + "/images")
                || uri.startsWith(ctx + "/assets")
                || uri.startsWith(ctx + "/uploads")
                || uri.startsWith(ctx + "/api/auth")
                || uri.equals(ctx + "/");
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object h) throws Exception {
        if (bypass(req)) return true;

        String ctx = req.getContextPath();
        String path = req.getRequestURI().substring(ctx.length()); // ex) "/inventory/list"

        if (!navGateService.isEnabledPath(path)) {
            // 숨기는 정책이면 404, 노출해도 된다고 보면 403으로 바꿔도 됨
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }
        return true;
    }
}
