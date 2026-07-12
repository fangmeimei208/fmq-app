package com.crypto.controller.system;

import com.crypto.entity.SysRole;
import com.crypto.entity.SysUser;
import com.crypto.service.AuthService;
import com.crypto.service.RoleService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    private final RoleService roleService;
    private final AuthService authService;

    public RoleController(RoleService roleService, AuthService authService) {
        this.roleService = roleService;
        this.authService = authService;
    }

    @GetMapping
    public Map<String, Object> list(HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        return Map.of("success", true, "data", roleService.findAll());
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable Long id, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        SysRole role = roleService.findById(id);
        List<Long> menuIds = roleService.getMenuIdsByRoleId(id);
        return Map.of("success", true, "data", role, "menuIds", menuIds);
    }

    @PostMapping
    public Map<String, Object> create(@RequestBody SysRole role, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        int n = roleService.create(role);
        return Map.of("success", n > 0, "message", n > 0 ? "创建成功" : "创建失败");
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody SysRole role, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        role.setId(id);
        int n = roleService.update(role);
        return Map.of("success", n > 0, "message", n > 0 ? "更新成功" : "更新失败");
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        if (id == 1L) {
            return Map.of("success", false, "message", "不能删除超级管理员角色");
        }
        int n = roleService.delete(id);
        return Map.of("success", n > 0, "message", n > 0 ? "删除成功" : "删除失败");
    }

    /**
     * 为角色分配菜单
     */
    @PostMapping("/{id}/menus")
    public Map<String, Object> assignMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body, HttpServletRequest request) {
        SysUser currentUser = authService.getCurrentUser(request);
        if (currentUser == null || currentUser.getIsAdmin() == null || !currentUser.getIsAdmin()) {
            return Map.of("success", false, "message", "无权限");
        }
        roleService.assignMenus(id, body.get("menuIds"));
        return Map.of("success", true, "message", "菜单分配成功");
    }
}
