package com.example.weboxbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weboxbackend.pojo.MenuItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MenuItemMapper extends BaseMapper<MenuItem> {
}
