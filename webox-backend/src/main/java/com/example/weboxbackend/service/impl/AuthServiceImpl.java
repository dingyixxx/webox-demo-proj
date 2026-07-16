package com.example.weboxbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weboxbackend.config.JwtUtil;
import com.example.weboxbackend.config.Md5Util;
import com.example.weboxbackend.dto.AuthRequest;
import com.example.weboxbackend.exception.BusinessException;
import com.example.weboxbackend.mapper.UserMapper;
import com.example.weboxbackend.pojo.User;
import com.example.weboxbackend.service.AuthService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    public AuthServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    public User register(AuthRequest request) {
        if (request.getEmail() == null || !request.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new BusinessException(400, "密码长度不能少于6位");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail())
                .eq(User::getIsDeleted, 0);
        if (userMapper.selectOne(wrapper) != null) {
            throw new BusinessException(400, "该邮箱已被注册");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(Md5Util.encrypt(request.getPassword()));
        user.setName(request.getName());
        user.setIsDeleted(0);
        user.setCreatedBy("system");
        user.setUpdatedBy("system");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    @Override
    public String login(AuthRequest request) {
        if (request.getEmail() == null || !request.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
            throw new BusinessException(400, "邮箱格式不正确");
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, request.getEmail())
                .eq(User::getIsDeleted, 0);
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException(400, "该邮箱未注册");
        }

        if (!user.getPassword().equals(Md5Util.encrypt(request.getPassword()))) {
            throw new BusinessException(400, "密码错误");
        }

        return JwtUtil.generateToken(user.getId(), user.getEmail());
    }

    @Override
    public User getLoginState(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new BusinessException(400, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }
}
