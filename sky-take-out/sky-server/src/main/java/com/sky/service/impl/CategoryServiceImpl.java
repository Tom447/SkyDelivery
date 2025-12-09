package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.exception.BusinessException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import com.sky.utils.BeanHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    @Override
    public Category getCategoryById(Long id) {

        Category category = categoryMapper.getCategoryById(id);
        if (Objects.isNull(category)){
            throw new BusinessException("该分类不存在");
        }
        return category;
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = BeanHelper.copyProperties(categoryDTO, Category.class);
        categoryMapper.update(category);
        return;
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        // 校验 status 是否合法
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值不合法");
        }
        //判断id对应的category是否可更新
        Category category = categoryMapper.getCategoryById(id);
        if (Objects.isNull(category)){
            throw new BusinessException("该分类不存在");
        }
        //分类存在。分类状态更新
        category.setStatus(status);
        category.setUpdateUser(BaseContext.getCurrentId());
        category.setUpdateTime(LocalDateTime.now());
        //更新分类
        categoryMapper.update(category);
        return;
    }

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = Category.builder()
                .id(categoryDTO.getId())
                .name(categoryDTO.getName())
                .sort(categoryDTO.getSort())
                .type(categoryDTO.getType())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .createUser(BaseContext.getCurrentId())
                .updateUser(BaseContext.getCurrentId())
                .status(StatusConstant.ENABLE)
                .build();
        categoryMapper.save(category);
        return;
    }

    @Override
    public void deleteById(Long id) {
        categoryMapper.deleteById(id);
        return;
    }

    @Override
    public List<Category> getByType(Integer type) {
        Category condition = Category.builder().type(type).build();
        List<Category> categories = categoryMapper.categoriesByCondition(condition);
        return categories;
    }
}
