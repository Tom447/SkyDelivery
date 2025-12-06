package com.sky.mapper;


import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {


    List<Dish> getDishsByIds(List<Long> ids);
}
