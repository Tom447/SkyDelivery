package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Employee;
import com.sky.exception.BusinessException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.EmployeeService;
import com.sky.utils.BeanHelper;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    //分页查询
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        //1.设置分页参数
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        //2.根据条件进行查询
        Dish condition = Dish.builder().name(dishPageQueryDTO.getName())
                .categoryId(dishPageQueryDTO.getCategoryId() != null ? dishPageQueryDTO.getCategoryId().longValue() : null)
                .status(dishPageQueryDTO.getStatus())
                .build();
        List<Dish> dishList = dishMapper.pageByCondition(condition);

        Page<Dish> page = (Page<Dish>) dishList;
        //3.解析并封装结果
        return new PageResult(page.getTotal(), page.getResult());
    }


    //批量删除
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        //得到status != 0的集合list
        List<Long> list = dishMapper.listDeletableIds(ids);
        //得到不可以删除的List：disableIds
        Set<Long> deleteIds = new HashSet<>(list);
        List<Long> disableIds = ids.stream().filter(id -> !deleteIds.contains(id)).collect(Collectors.toList());
        //判断不可删除的
        if (!disableIds.isEmpty()){
            throw new BusinessException("含有不可删除的菜品");
        }
        //删除dish中的list标记的元素
        dishMapper.delete(list);
        //删除dish相关联的表dish_flavor的list中的元素
        dishFlavorMapper.deleteByDishIds(list);
    }

    @Override
    @Transactional
    public DishVO getDishById(Long id) {
        //通过id获取对应的dish
        List<Dish> dishList = dishMapper.getDishsByIds(Arrays.asList(id));
        //如果该id不存在就抛异常
        if (Objects.isNull(dishList)){
            throw new BusinessException("该菜品不存在");
        }
        //id存在得到查到的dish
        Dish dish = dishList.get(0);
        DishVO dishVO = BeanHelper.copyProperties(dish, DishVO.class);
        //通过id查到对应的flavors
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorsByDishId(id);
        //将查到的flavors放到DishVO里
        dishVO.setFlavors(flavors);
        return dishVO;
    }
}
