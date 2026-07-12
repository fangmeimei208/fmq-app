package com.crypto.interceptor;

import com.crypto.entity.SysUser;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    // 无需登录的路径
    private static final String[] WHITE_LIST = {
        "/api/auth/login",
        "/api/auth/logout",
        "/login.html",
        "/favicon.ico",
        "/error"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 白名单放行
        for (String white : WHITE_LIST) {
            if (path.equals(white) || path.equals(request.getContextPath() + white)) {
                return true;
            }
        }

        // 静态资源放行 (.js, .css, .png, .ico 等)
        if (path.contains(".")) {
            return true;
        }

        // API 请求需要登录
        if (path.startsWith("/api/")) {
            SysUser user = (SysUser) request.getSession().getAttribute("user");
            if (user == null) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(401);
                response.getWriter().write("{\"success\":false,\"message\":\"请先登录\",\"code\":401}");
                return false;
            }
        }

        return true;
    }
}
