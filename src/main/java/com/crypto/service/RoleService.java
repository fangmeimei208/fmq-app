package com.crypto.service;

import com.crypto.entity.SysRole;
import com.crypto.mapper.SysMenuMapper;
import com.crypto.mapper.SysRoleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;

    public RoleService(SysRoleMapper sysRoleMapper, SysMenuMapper sysMenuMapper) {
        this.sysRoleMapper = sysRoleMapper;
        this.sysMenuMapper = sysMenuMapper;
    }

    public List<SysRole> findAll() {
        return sysRoleMapper.findAll();
    }

    public SysRole findById(Long id) {
        return sysRoleMapper.findById(id);
    }

    public int create(SysRole role) {
        return sysRoleMapper.insert(role);
    }

    public int update(SysRole role) {
        return sysRoleMapper.update(role);
    }

    public int delete(Long id) {
        return sysRoleMapper.delete(id);
    }

    /**
     * 为角色分配菜单
     */
    public void assignMenus(Long roleId, List<Long> menuIds) {
        sysMenuMapper.deleteRoleMenus(roleId);
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                sysMenuMapper.insertRoleMenu(roleId, menuId);
            }
        }
    }

    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return sysMenuMapper.findMenuIdsByRoleId(roleId);
    }
}
