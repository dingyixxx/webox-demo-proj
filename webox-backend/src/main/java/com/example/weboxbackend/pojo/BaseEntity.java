package com.example.weboxbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity {
    private String id;
    private Integer isDeleted;    // 逻辑删除 0-未删除 1-已删除
    private String createdBy;     // 创建人
    private String updatedBy;     // 更新人
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 更新时间
}
