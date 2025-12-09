package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.EmployeeService;
import com.sky.utils.BeanHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Override
    @Transactional
    public void save(DishDTO dishDTO) {
        //获取dishDTO中的要插入的dish属性
        Dish dish = BeanHelper.copyProperties(dishDTO, Dish.class);
        //设置需要更新的部分
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        //插入到dish表里，并且得到有dish_id的dish
        dishMapper.save(dish);
        //得到要插入的dish_flavor部分
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //向表dish_flavor中插入flavor
        flavors.stream().forEach(dishFlavor -> {
            //插入前设置dish_id
            dishFlavor.setDishId(dish.getId());
            //插入
            dishFlavorMapper.save(dishFlavor);
        });
        return;
    }
}
