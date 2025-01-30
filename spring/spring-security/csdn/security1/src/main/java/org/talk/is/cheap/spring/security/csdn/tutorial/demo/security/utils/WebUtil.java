package org.talk.is.cheap.spring.security.csdn.tutorial.demo.security.utils;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class WebUtil {
    public static String renderString(HttpServletResponse resp, String s) {
        try {
            resp.setStatus(200);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("utf-8");
            resp.getWriter().print(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
