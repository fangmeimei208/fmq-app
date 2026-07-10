package com.crypto.entity;

public class SysRoleMenu {
    private Long id;
    private Long roleId;
    private Long menuId;

    public SysRoleMenu() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }
}
