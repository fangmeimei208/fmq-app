package com.crypto.service;

import com.crypto.entity.SysMenu;
import com.crypto.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MenuService {

    private final SysMenuMapper sysMenuMapper;

    public MenuService(SysMenuMapper sysMenuMapper) {
        this.sysMenuMapper = sysMenuMapper;
    }

    public List<SysMenu> findAll() {
        return sysMenuMapper.findAll();
    }

    public SysMenu findById(Long id) {
        return sysMenuMapper.findById(id);
    }

    public int create(SysMenu menu) {
        return sysMenuMapper.insert(menu);
    }

    public int update(SysMenu menu) {
        return sysMenuMapper.update(menu);
    }

    public int delete(Long id) {
        return sysMenuMapper.delete(id);
    }

    /**
     * 获取完整菜单树（包含所有启用的菜单）
     */
    public List<Map<String, Object>> getMenuTree() {
        List<SysMenu> allMenus = sysMenuMapper.findAll();

        // 分离一级和二级
        List<SysMenu> topLevel = new ArrayList<>();
        Map<Long, List<SysMenu>> childrenMap = new LinkedHashMap<>();

        for (SysMenu menu : allMenus) {
            if (menu.getParentId() == null || menu.getParentId() == 0) {
                topLevel.add(menu);
            } else {
                childrenMap.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu);
            }
        }

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
}
