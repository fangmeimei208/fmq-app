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
            // 收集用户所有可访问的 menuCode
            Set<String> allowedCodes = collectAllowedCodes(menus);
            // 通过 menuCode 映射 API 前缀
            if (!isApiAllowed(path, allowedCodes)) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(403);
                response.getWriter().write("{\"success\":false,\"message\":\"无权限访问此功能，请联系管理员\",\"code\":403}");
                return false;
            }
        }

        return true;
    }

    // menuCode -> 允许的API路径前缀映射
    private static final Map<String, List<String>> API_PREFIX_MAP = new HashMap<>();
    static {
        API_PREFIX_MAP.put("express_token", Arrays.asList("/api/expressToken/"));
        API_PREFIX_MAP.put("sinotrans_aes", Arrays.asList("/api/sinotrans/"));
        API_PREFIX_MAP.put("pg_as2", Arrays.asList("/api/pg/"));
        API_PREFIX_MAP.put("jlzy_socket", Arrays.asList("/api/jlzy/", "/api/sendMsg/"));
        API_PREFIX_MAP.put("fulle_share", Arrays.asList("/api/fulle/"));
        API_PREFIX_MAP.put("wechat_push", Arrays.asList("/api/wechat-push/"));
        API_PREFIX_MAP.put("user_mgmt", Arrays.asList("/api/users/"));
        API_PREFIX_MAP.put("role_mgmt", Arrays.asList("/api/roles/"));
        API_PREFIX_MAP.put("login_log", Arrays.asList("/api/menus/login-logs"));
    }

    private Set<String> collectAllowedCodes(List<SysMenu> menus) {
        Set<String> codes = new HashSet<>();
        for (SysMenu menu : menus) {
            if (menu.getMenuCode() != null) {
                codes.add(menu.getMenuCode());
            }
        }
        return codes;
    }

    private boolean isApiAllowed(String path, Set<String> allowedCodes) {
        for (String code : allowedCodes) {
            List<String> prefixes = API_PREFIX_MAP.get(code);
            if (prefixes != null) {
                for (String prefix : prefixes) {
                    if (path.startsWith(prefix)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
