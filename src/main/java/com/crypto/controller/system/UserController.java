package com.crypto.controller.system;

import com.crypto.entity.SysUser;
import com.crypto.service.UserService;
import com.crypto.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @GetMapping
    public Map<String, Object> list(HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        List<SysUser> users = userService.findAll();
        // 去掉密码字段
        for (SysUser u : users) {
            u.setPassword(null);
        }
        return Map.of("success", true, "data", users);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable Long id, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        SysUser user = userService.findById(id);
        if (user != null) user.setPassword(null);
        return Map.of("success", true, "data", user);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody SysUser user, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        int n = userService.create(user);
        return Map.of("success", n > 0, "message", n > 0 ? "创建成功" : "创建失败");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody SysUser user, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        user.setId(id);
        int n = userService.update(user);
        return Map.of("success", n > 0, "message", n > 0 ? "更新成功" : "更新失败");
    }

    @PutMapping("/{id}/reset-password")
    public Map<String, Object> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        int n = userService.resetPassword(id, body.get("password"));
        return Map.of("success", n > 0, "message", n > 0 ? "密码重置成功" : "重置失败");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        if (id == 1L) {
            return Map.of("success", false, "message", "不能删除超级管理员");
        }
        int n = userService.delete(id);
        return Map.of("success", n > 0, "message", n > 0 ? "删除成功" : "删除失败");
    }
}
