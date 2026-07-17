package com.example.weboxbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_order")
public class Order extends BaseEntity{
    private Long userId;          // 下单用户
    private String email;       // 登录邮箱
    private String name;        // 用户姓名
    private Integer totalAmount;     // 订单总额
    private LocalDate deliveryDate;    // 配送日期 "YYYY-MM-DD"
    private String mealPeriod;      // "lunch" | "dinner"
    private String deliveryAddress; // 配送地址
    private String status;          // "pending" | "confirmed" | "completed" | "cancelled"
}
