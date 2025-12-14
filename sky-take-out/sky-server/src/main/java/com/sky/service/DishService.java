package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    void save(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    void delete(List<Long> list);

    DishVO getDishById(Long id);

    void update(DishDTO dishDTO);

    void updateStatus(Long id, Integer status);

    List<Dish> getDishByCondition(Long categoryId, String name);

    List<DishVO> listDishsWithFlavors(Long categoryId);
}
