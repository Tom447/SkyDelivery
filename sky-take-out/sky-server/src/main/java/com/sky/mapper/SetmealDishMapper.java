package com.sky.mapper;


import com.sky.entity.SetmealDish;
import com.sky.vo.SetmealDishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    @Insert("insert into setmeal_dish(setmeal_id, dish_id, name, price, copies)" +
            "values (#{setmealId},#{dishId},#{name},#{price},#{copies})")
    void save(SetmealDish setmealDish);

    //批量删除
    void deleteBySetmealIds(List<Long> ids);


    @Select("select * from setmeal_dish where setmeal_id=#{setmealId}")
    List<SetmealDish> getDishsBySetmealId(Long setmealId);

    List<SetmealDishVO> getDishBySetmealId(Long setmealId);
}
