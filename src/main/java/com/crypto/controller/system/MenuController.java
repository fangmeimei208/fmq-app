package com.crypto.controller.system;

import com.crypto.entity.SysUser;
import com.crypto.service.AuthService;
import com.crypto.service.MenuService;
import com.crypto.mapper.LoginLogRepository;
import org.bson.Document;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;
    private final AuthService authService;
    private final LoginLogRepository loginLogRepository;

    public MenuController(MenuService menuService, AuthService authService,
                          LoginLogRepository loginLogRepository) {
        this.menuService = menuService;
        this.authService = authService;
        this.loginLogRepository = loginLogRepository;
    }

    @GetMapping
    public Map<String, Object> list(HttpServletRequest request) {
        return Map.of("success", true, "data", menuService.findAll());
    }

    @GetMapping("/login-logs")
    public Map<String, Object> loginLogs(HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        List<Document> logs = loginLogRepository.findRecent(200);
        return Map.of("success", true, "data", logs);
    }
}
