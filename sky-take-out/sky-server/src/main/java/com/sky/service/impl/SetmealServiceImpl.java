package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import com.sky.utils.BeanHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {


    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;




    //保存套餐
    @Override
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanHelper.copyProperties(setmealDTO, Setmeal.class);
        System.out.println(setmeal);
        setmeal.setCreateTime(LocalDateTime.now());
        setmeal.setUpdateTime(LocalDateTime.now());
        System.out.println(BaseContext.getCurrentId());
        setmeal.setCreateUser(BaseContext.getCurrentId());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        System.out.println(setmeal);
        //保存套餐的信息
        Long  setmealId = setmealMapper.save(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //保存套餐和菜品关系的信息
        setmealDishes.stream().forEach(setmealDish->{
            setmealDish.setSetmealId(setmealId);
            setmealDishMapper.save(setmealDish);
        });
    }
}
