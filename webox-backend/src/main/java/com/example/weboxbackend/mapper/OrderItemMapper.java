package com.example.weboxbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weboxbackend.pojo.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}