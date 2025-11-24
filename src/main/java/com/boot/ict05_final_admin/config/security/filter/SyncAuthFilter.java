package com.boot.ict05_final_admin.config.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

public class SyncAuthFilter extends OncePerRequestFilter {

    private final String sharedSecret;

    public SyncAuthFilter(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String path = req.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        if (path.startsWith("/API/receive/")) {
            String token = req.getHeader("X-Sync-Auth");
            if (token == null || !token.equals(sharedSecret)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("Missing or invalid X-Sync-Auth");
                return;
            }
        }

        chain.doFilter(req, res);
    }
}
