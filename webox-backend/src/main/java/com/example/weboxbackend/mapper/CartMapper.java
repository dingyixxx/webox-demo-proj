package com.example.weboxbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.weboxbackend.pojo.Cart;
import com.example.weboxbackend.vo.CartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    @Results({
            @Result(column = "allergens", property = "allergens", typeHandler = JacksonTypeHandler.class)
    })
    @Select("SELECT c.id, c.user_id AS userId, c.menu_item_id AS menuItemId, c.quantity, " +
            "mi.name, mi.image, mi.category, mi.allergens, h.price, " +
            "h.price * c.quantity AS subtotal " +
            "FROM t_cart c " +
            "JOIN t_menu_item mi ON c.menu_item_id = mi.id AND mi.is_deleted = 0 " +
            "JOIN t_menu_item_history h ON h.menu_item_id = mi.id " +
            "AND h.valid_from_date = (" +
            "  SELECT MAX(h2.valid_from_date) FROM t_menu_item_history h2 " +
            "  WHERE h2.menu_item_id = mi.id AND h2.valid_from_date <= CURDATE() AND h2.is_deleted = 0" +
            ") " +
            "WHERE c.user_id = #{userId} AND c.is_deleted = 0 " +
            "ORDER BY c.id")
    List<CartVO> selectUserCart(@Param("userId") Long userId);
}
