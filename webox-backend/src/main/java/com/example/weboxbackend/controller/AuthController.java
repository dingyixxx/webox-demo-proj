package com.example.weboxbackend.controller;

import com.example.weboxbackend.config.JwtUtil;
import com.example.weboxbackend.dto.AuthRequest;
import com.example.weboxbackend.dto.Result;
import com.example.weboxbackend.pojo.User;
import com.example.weboxbackend.service.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody AuthRequest request) {
        User user = authService.register(request);
        return Result.success(user);
    }

    @PostMapping("/login")
    public Result<String> login(@RequestBody AuthRequest request) {
        String token = authService.login(request);
        return Result.success(token);
    }

    @GetMapping("/me")
    public Result<User> getLoginState(@RequestHeader("token") String token) {
        Claims claims = JwtUtil.parseToken(token);
        Long userId = Long.parseLong(claims.getSubject());
        User user = authService.getLoginState(userId);
        return Result.success(user);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
