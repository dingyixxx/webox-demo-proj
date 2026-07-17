package com.example.weboxbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "t_user", autoResultMap = true)
public class User extends DietaryInfo{
    private String email;       // 登录邮箱
    private String password;    // 密码（加密存储）
    private String name;        // 用户姓名
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> categories;    // 偏好分类（多选, 如 ["chinese", "japanese"]）
    private int preferredMinPrice;      // 偏好起始价格（分）
    private int preferredMaxPrice;      // 偏好终止价格（分）

}
