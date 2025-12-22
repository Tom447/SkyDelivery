package com.sky.service;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.result.PageResult;
import com.sky.vo.SetmealDishVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;


public interface SetmealService {

    void save(SetmealDTO setmealDTO);


    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    void delete(List<Long> ids);

    SetmealVO getSetmealById(Long id);

    void update(SetmealDTO setmealDTO);

    void updateStatus(Long id, Integer status);

    List<Setmeal> list(Setmeal setmeal);

    List<SetmealDishVO> getDishBySetmealId(Long setmealId);
}
