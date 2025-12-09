package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {


    @Autowired
    private CategoryMapper categoryMapper;

    //分页查询
    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        //1.设置分页参数
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());

        //2.根据条件进行查询
        Category condition = Category.builder().name(categoryPageQueryDTO.getName())
                .type(categoryPageQueryDTO.getType())
                .build();

        List<Category> categoriesList = categoryMapper.categoriesByCondition(condition);

        Page<Category> page = (Page<Category>) categoriesList;
        //3.解析并封装结果
        return new PageResult(page.getTotal(), page.getResult());
    }
}
