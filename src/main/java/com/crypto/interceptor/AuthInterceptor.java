package com.crypto.interceptor;

import com.crypto.entity.SysMenu;
import com.crypto.entity.SysUser;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    // 无需登录的路径
    private static final Set<String> WHITE_LIST = new HashSet<>(Arrays.asList(
        "/api/auth/login",
        "/api/auth/logout",
        "/api/auth/current-user",
        "/login.html",
        "/favicon.ico",
        "/error"
    ));

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        // 白名单放行
        if (WHITE_LIST.contains(path)) {
            return true;
        }

        // 静态资源放行 (.js, .css, .png, .ico 等)
        if (path.contains(".")) {
            return true;
        }

        // 需要登录校验
        SysUser user = (SysUser) request.getSession().getAttribute("user");
        if (user == null) {
            if (path.startsWith("/api/")) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(401);
                response.getWriter().write("{\"success\":false,\"message\":\"请先登录\",\"code\":401}");
            } else {
                response.sendRedirect("/login.html");
            }
            return false;
        }

        // 管理员跳过权限校验（拥有所有权限）
        if (user.getIsAdmin() != null && user.getIsAdmin()) {
            return true;
        }

        // 页面访问权限校验：检查用户是否有该页面的菜单权限
        @SuppressWarnings("unchecked")
        List<SysMenu> menus = (List<SysMenu>) request.getSession().getAttribute("menus");
        if (menus != null && path.startsWith("/api/") && !path.startsWith("/api/auth/")) {
            // 收集用户所有可访问的 URL
            Set<String> allowedUrls = collectAllowedUrls(menus);
            // 检查当前请求路径是否在允许列表中
            // API 路径匹配：如 /api/express/... 对应菜单 url /express/...
            String checkPath = path.replaceFirst("^/api", "");
            boolean allowed = false;
            for (String allowedUrl : allowedUrls) {
                if (allowedUrl != null && !allowedUrl.isEmpty() && checkPath.startsWith(allowedUrl)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(403);
                response.getWriter().write("{\"success\":false,\"message\":\"无权限访问此功能，请联系管理员\",\"code\":403}");
                return false;
            }
        }

        return true;
    }

    /**
     * 收集菜单树中所有叶子节点（有url的页面）的URL
     */
    private Set<String> collectAllowedUrls(List<SysMenu> menus) {
        Set<String> urls = new HashSet<>();
        for (SysMenu menu : menus) {
            if (menu.getUrl() != null && !menu.getUrl().isEmpty()) {
                urls.add(menu.getUrl());
            }
        }
        return urls;
    }
}
