package com.sky.mapper;


import com.sky.entity.Category;
import com.sky.entity.Dish;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;

@Mapper
public interface CategoryMapper {


    List<Category> categoriesByCondition(Category condition);
}

