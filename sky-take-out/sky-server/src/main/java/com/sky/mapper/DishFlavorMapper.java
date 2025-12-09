package com.sky.mapper;


import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    @Insert("insert into dish_flavor(dish_id, name, value) " +
            "value(#{dishId},#{name},#{value}) ")
    void save(DishFlavor dishFlavor);

    void deleteByDishIds(List<Long> list);


    @Select("select id, dish_id, name, value from dish_flavor where dish_id=#{dishId}")
    List<DishFlavor> getFlavorsByDishId(Long dishId);
}
