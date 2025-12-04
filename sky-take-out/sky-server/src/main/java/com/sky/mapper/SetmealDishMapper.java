package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SetmealDishMapper {

    @Insert("insert into setmeal_dish(setmeal_id, dish_id, name, price, copies)" +
            "values (#{setmealId},#{dishId},#{name},#{price},#{copies})")
    void save(SetmealDish setmealDish);
}
