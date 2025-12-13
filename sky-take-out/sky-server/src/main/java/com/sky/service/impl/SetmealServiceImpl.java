package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.BusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.utils.BeanHelper;
import com.sky.vo.SetmealVO;
import javassist.expr.NewArray;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl implements SetmealService {


    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 保存套餐信息
     * @param setmealDTO
     */
    @Override
    @Transactional
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
        setmealMapper.save(setmeal);
        //保存套餐关联表信息
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        //保存套餐和菜品关系的信息
        setmealDishes.stream().forEach(setmealDish->{
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishMapper.save(setmealDish);
        });
    }


    //套餐分页查询
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Setmeal condition= Setmeal.builder().name(setmealPageQueryDTO.getName())
                .categoryId(setmealPageQueryDTO.getCategoryId() != null ? setmealPageQueryDTO.getCategoryId().longValue() : null)
                .status(setmealPageQueryDTO.getStatus())
                .build();

        List<Setmeal> list = setmealMapper.pageByCondition(condition);
        Page<Setmeal> setmealList = (Page<Setmeal>) list;
        if (setmealList.getTotal() == 0){
            throw new BusinessException("未找到相关套餐");
        }
        return new PageResult(setmealList.getTotal(), setmealList.getResult());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
//        //得到status != 0的集合list
//        List<Long> list = setmealMapper.listDeletableIds(ids);
//        Set<Long> deleteIds = new HashSet<>(list);
//        List<Long> disableIds = ids.stream().filter(id -> !deleteIds.contains(id)).collect(Collectors.toList());
//
//        if (!disableIds.isEmpty()){
//            throw new BusinessException("启售套餐不可被删除");
//        }
//        //删除setmeal中的list标记的元素
//        setmealMapper.delete(list);
//        //删除setmeal相关联的表setmeal_dish的list中的元素
//        setmealDishMapper.deleteBySetmealIds(list);
        ids.stream().forEach(id ->{
            updateStatus(id, StatusConstant.ENABLE);
        });
        // setmeal_dish 不做任何操作！
        // 前端查套餐时，只查 status=1 的，setmeal_dish 自动关联
    }

    @Override
    public SetmealVO getSetmealById(Long id) {
        Setmeal setmeal = setmealMapper.getSetmealById(id);
        if (Objects.isNull(setmeal)){
            throw new BusinessException("该套餐不存在");
        }
        SetmealVO setmealVO = BeanHelper.copyProperties(setmeal, SetmealVO.class);
        List<SetmealDish> setmealDishList = setmealDishMapper.getDishsBySetmealId(setmeal.getId());
        setmealVO.setSetmealDishes(setmealDishList);
        return setmealVO;
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = BeanHelper.copyProperties(setmealDTO, Setmeal.class);
        setmeal.setUpdateTime(LocalDateTime.now());
        setmeal.setUpdateUser(BaseContext.getCurrentId());
        setmealMapper.update(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.stream().forEach(setmealDish -> {
           setmealDish.setSetmealId(setmeal.getId());
        });
        //将旧的setmeal对应的dishs删除
        setmealDishMapper.deleteBySetmealIds(Arrays.asList(setmeal.getId()));
        //插入新的
        setmealDishes.stream().forEach(setmealDish -> {
            setmealDishMapper.save(setmealDish);
        });
        return;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        // 校验 status 是否合法
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值不合法");
        }

        // 获取该套餐的所有菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getDishsBySetmealId(id);
        if (setmealDishes.isEmpty()) {
            throw new BusinessException("套餐未关联任何菜品");
        }

        // 获取所有菜品 ID
        List<Long> dishIds = setmealDishes.stream()
                .map(SetmealDish::getDishId)
                .collect(Collectors.toList());

        // 获取菜品列表
        List<Dish> dishList = dishMapper.getDishsByIds(dishIds);

        // 判断是否包含停售菜品（状态为 0）
        boolean hasStoppedDish = dishList.stream()
                .anyMatch(dish -> dish.getStatus() == 0);

        // 如果要起售（status=0），但包含停售菜品，则禁止
        if (status == 1 && hasStoppedDish) {
            throw new BusinessException("套餐包含停售菜品，无法起售");
        }

        // 更新套餐状态
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .updateTime(LocalDateTime.now())
                .updateUser(BaseContext.getCurrentId())
                .build();
        setmealMapper.update(setmeal);
        return;
    }

    @Override
    public List<Setmeal> getSetmealByCategoryId(Long categoryId) {
        List<Setmeal> setmealByCategoryId = setmealMapper.getSetmealByCategoryId(categoryId);
        return setmealByCategoryId;
    }
}
