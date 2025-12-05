package com.sky.mapper;


import com.sky.entity.Setmeal;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {


    @Insert("insert into setmeal(name, category_id, price, status, description, image, create_time, update_time, create_user, update_user)" +
            "values (#{name},#{categoryId},#{price},#{status},#{description},#{image},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    Long save(Setmeal setmeal);

    List<Setmeal> pageByCondition(Setmeal setmeal);
}
