package com.sky.mapper;


import com.sky.entity.Category;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {


    List<Category> categoriesByCondition(Category condition);

    @Select("select id, type, name, sort, status, create_time, update_time, create_user, update_user from category where id = #{id}")
    Category getCategoryById(Long id);

    void update(Category category);

    @Insert("INSERT INTO category(type, name, sort, status, create_time, update_time, create_user, update_user) " +
            "VALUES(#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void save(Category category);
}

