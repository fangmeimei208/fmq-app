package com.crypto.service;

import com.crypto.entity.SysUser;
import com.crypto.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final SysUserMapper sysUserMapper;

    public UserService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    public List<SysUser> findAll() {
        return sysUserMapper.findAll();
    }

    public SysUser findById(Long id) {
        return sysUserMapper.findById(id);
    }

    public int create(SysUser user) {
        // BCrypt 加密密码
        user.setPassword(org.mindrot.jbcrypt.BCrypt.hashpw(user.getPassword(), org.mindrot.jbcrypt.BCrypt.gensalt()));
        return sysUserMapper.insert(user);
    }

    public int update(SysUser user) {
        return sysUserMapper.update(user);
    }

    public int resetPassword(Long id, String newPassword) {
        String encoded = org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());
        return sysUserMapper.updatePassword(id, encoded);
    }

    public int delete(Long id) {
        return sysUserMapper.delete(id);
    }
}
