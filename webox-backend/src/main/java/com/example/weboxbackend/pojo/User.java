package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User extends DietaryInfo{
    private String email;       // 登录邮箱
    private String password;    // 密码（加密存储）
    private String name;        // 用户姓名
    private List<String> categories;    // 偏好分类（多选, 如 ["chinese", "japanese"]）
}
