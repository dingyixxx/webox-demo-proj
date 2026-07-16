package com.example.weboxbackend.service;

import com.example.weboxbackend.dto.AuthRequest;
import com.example.weboxbackend.pojo.User;

public interface AuthService {
    User register(AuthRequest request);

    String login(AuthRequest request);

    User getLoginState(Long userId);
}
