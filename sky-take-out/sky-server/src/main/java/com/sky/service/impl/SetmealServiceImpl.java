package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.utils.BeanHelper;
import com.sky.vo.SetmealVO;
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


    /**
     * 保存套餐信息
     * @param setmealDTO
     */
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


    //套餐分页查询
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Setmeal setmeal = Setmeal.builder().name(setmealPageQueryDTO.getName())
                .categoryId(setmealPageQueryDTO.getCategoryId() != null ? setmealPageQueryDTO.getCategoryId().longValue() : null)
                .status(setmealPageQueryDTO.getStatus())
                .build();

        List<Setmeal> list = setmealMapper.pageByCondition(setmeal);
        Page<Setmeal> setmealList = (Page<Setmeal>) list;
        return new PageResult(setmealList.getTotal(), setmealList.getResult());
    }
}
