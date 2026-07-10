package com.crypto.controller.system;

import com.crypto.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.crypto.entity.SysUser;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");
        return authService.login(username, password, request);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request) {
        authService.logout(request);
        return Map.of("success", true, "message", "已退出登录");
    }

    @GetMapping("/current-user")
    public Map<String, Object> currentUser(HttpServletRequest request) {
        SysUser user = authService.getCurrentUser(request);
        if (user == null) {
            return Map.of("success", false, "message", "未登录", "code", 401);
        }
        List<Map<String, Object>> menus = authService.getCurrentMenus(request);
        return Map.of("success", true, "user", user, "menus", menus);
    }
}
