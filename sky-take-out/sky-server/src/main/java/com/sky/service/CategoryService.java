package com.sky.service;


import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

public interface CategoryService {


    PageResult page(CategoryPageQueryDTO categoryDTO);

    Category getCategoryById(Long id);
}
