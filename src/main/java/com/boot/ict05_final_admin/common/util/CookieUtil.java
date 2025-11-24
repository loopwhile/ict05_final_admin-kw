package com.boot.ict05_final_admin.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {
    public static void write(HttpServletResponse res, String name, String val,
                             String domain, boolean secure, int maxAge) {
        Cookie c = new Cookie(name, val);
        c.setHttpOnly(true); c.setSecure(secure); c.setPath("/"); c.setMaxAge(maxAge);
        if (domain != null && !domain.isBlank()) c.setDomain(domain);
        res.addCookie(c);
    }
    public static String read(HttpServletRequest req, String name) {
        var cs = req.getCookies(); if (cs == null) return null;
        for (var c : cs) if (name.equals(c.getName())) return c.getValue();
        return null;
    }
    public static void clear(HttpServletResponse res, String name, String domain, boolean secure) {
        write(res, name, "", domain, secure, 0);
    }
}

