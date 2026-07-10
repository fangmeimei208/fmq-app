package com.crypto.service;

import com.crypto.entity.SysMenu;
import com.crypto.mapper.SysMenuMapper;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
