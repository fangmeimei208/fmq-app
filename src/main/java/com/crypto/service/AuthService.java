package com.crypto.service;

import com.crypto.entity.SysMenu;
import com.crypto.entity.SysUser;
import com.crypto.mapper.LoginLogRepository;
import com.crypto.mapper.SysMenuMapper;
import com.crypto.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

@Service
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysMenuMapper sysMenuMapper;
    private final LoginLogRepository loginLogRepository;

    public AuthService(SysUserMapper sysUserMapper, SysMenuMapper sysMenuMapper,
                       LoginLogRepository loginLogRepository) {
        this.sysUserMapper = sysUserMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.loginLogRepository = loginLogRepository;
    }

    /**
     * 登录校验
     */
    public Map<String, Object> login(String username, String password, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        SysUser user = sysUserMapper.findByUsername(username);
        if (user == null) {
            loginLogRepository.insert(null, username, getClientIp(request),
                request.getHeader("User-Agent"), "FAIL", "用户不存在");
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            loginLogRepository.insert(user.getId(), username, getClientIp(request),
                request.getHeader("User-Agent"), "FAIL", "账号已禁用");
            result.put("success", false);
            result.put("message", "账号已被禁用");
            return result;
        }

        if (!checkPassword(password, user.getPassword())) {
            loginLogRepository.insert(user.getId(), username, getClientIp(request),
                request.getHeader("User-Agent"), "FAIL", "密码错误");
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 更新登录信息
        String ip = getClientIp(request);
        sysUserMapper.updateLoginInfo(user.getId(), ip);

        // 记录登录日志
        loginLogRepository.insert(user.getId(), username, ip,
            request.getHeader("User-Agent"), "SUCCESS", null);

        // 获取用户的菜单权限
        List<SysMenu> menus;
        if (user.getIsAdmin() != null && user.getIsAdmin()) {
            menus = sysMenuMapper.findAll();
        } else if (user.getRoleId() != null) {
            menus = sysMenuMapper.findByRoleId(user.getRoleId());
        } else {
            menus = Collections.emptyList();
        }

        // 存入session
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("menus", menus);

        // 构建返回数据（不含密码）
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("isAdmin", user.getIsAdmin());
        userInfo.put("roleId", user.getRoleId());

        result.put("success", true);
        result.put("user", userInfo);
        result.put("menus", buildMenuTree(menus));
        return result;
    }

    public SysUser getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (SysUser) session.getAttribute("user");
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getCurrentMenus(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            List<SysMenu> menus = (List<SysMenu>) session.getAttribute("menus");
            if (menus != null) {
                return buildMenuTree(menus);
            }
        }
        return Collections.emptyList();
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private boolean checkPassword(String rawPassword, String encodedPassword) {
        return org.mindrot.jbcrypt.BCrypt.checkpw(rawPassword, encodedPassword);
    }

    /**
     * 构建两级菜单树：一级菜单(parentId=0)包含二级菜单(parentId指向一级)
     */
    private List<Map<String, Object>> buildMenuTree(List<SysMenu> menus) {
        // 分离一级和二级菜单
        List<SysMenu> topLevel = new ArrayList<>();
        Map<Long, List<SysMenu>> childrenMap = new LinkedHashMap<>();

        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                topLevel.add(menu);
            } else {
                childrenMap.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu);
            }
        }

        // 排序一级菜单
        topLevel.sort(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 999));

        List<Map<String, Object>> result = new ArrayList<>();
        for (SysMenu parent : topLevel) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", parent.getId());
            item.put("menuName", parent.getMenuName());
            item.put("menuCode", parent.getMenuCode());
            item.put("url", parent.getUrl());
            item.put("icon", parent.getIcon());
            item.put("sortOrder", parent.getSortOrder());

            // 构建子菜单
            List<SysMenu> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
            children.sort(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 999));

            if (!children.isEmpty()) {
                List<Map<String, Object>> childList = new ArrayList<>();
                for (SysMenu child : children) {
                    Map<String, Object> childItem = new LinkedHashMap<>();
                    childItem.put("id", child.getId());
                    childItem.put("menuName", child.getMenuName());
                    childItem.put("menuCode", child.getMenuCode());
                    childItem.put("url", child.getUrl());
                    childItem.put("icon", child.getIcon());
                    childItem.put("sortOrder", child.getSortOrder());
                    childItem.put("parentId", child.getParentId());
                    childList.add(childItem);
                }
                item.put("children", childList);
            }
            result.add(item);
        }
        return result;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
