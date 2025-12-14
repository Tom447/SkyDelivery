package com.sky.mapper;


import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {


    List<Dish> getDishsByIds(List<Long> ids);

    @Insert("insert into dish(name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) " +
            "values (#{name},#{categoryId},#{price},#{image},#{description},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long save(Dish dish);

    List<Dish> dishsByCondition(Dish condition);

    List<Long> listDeletableIds(List<Long> ids);


    void delete(List<Long> ids);

    void update(Dish dish);

    List<DishVO> listDishsWithFlavors(Dish dish);
}

