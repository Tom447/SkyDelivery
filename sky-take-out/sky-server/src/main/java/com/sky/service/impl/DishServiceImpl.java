package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.entity.*;
import com.sky.exception.BusinessException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.EmployeeService;
import com.sky.utils.BeanHelper;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

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

        //3.删除redis缓存中的菜品数据
        //TODO...
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
        List<Dish> dishList = dishMapper.dishsByCondition(condition);

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
            throw new BusinessException("启售的菜品不可删除");
        }
        //删除dish中的list标记的元素
        dishMapper.delete(list);
        //删除dish相关联的表dish_flavor的list中的元素
        dishFlavorMapper.deleteByDishIds(list);

        //软删除
//        ids.stream().forEach(id -> {
//            updateStatus(id, StatusConstant.DISABLE);
//        });

        //删除redis缓存中的菜单数据
        //TODO...
    }

    @Override
    @Transactional
    public DishVO getDishById(Long id) {
        //通过id获取对应的dish
        List<Dish> dishList = dishMapper.getDishsByIds(Arrays.asList(id));
        //如果该id不存在就抛异常
        if (Objects.isNull(dishList)) {
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

    @Override
    @Transactional
    public void update(DishDTO dishDTO) {

        //通过dishDTO获得dish
        Dish dish = BeanHelper.copyProperties(dishDTO, Dish.class);
        dish.setUpdateTime(LocalDateTime.now());
        dish.setUpdateUser(BaseContext.getCurrentId());
        //更新dish表
        dishMapper.update(dish);
        //通过dishDTO得到dish_flavors
        List<DishFlavor> flavors = dishDTO.getFlavors();
        //将dishDTO中的dish_id的旧的flavor删除掉
        dishFlavorMapper.deleteByDishIds(Arrays.asList(dish.getId()));
        //插入新的
        flavors.stream().forEach(dishFlavor -> {
            dishFlavor.setDishId(dish.getId());
            dishFlavorMapper.save(dishFlavor);
        });


        //删除redis缓存中的菜单数据
        //TODO...
        return;
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status) {
        //1.更新菜品
        Dish dish = Dish.builder().id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        //2.如果是停售操作，还需要将该菜品关联的套餐也停售了
        if (status == StatusConstant.DISABLE){
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(Collections.singletonList(id));
            if (!CollectionUtils.isEmpty(setmealIds)){
                setmealIds.stream().forEach(setmealId ->{
                    Setmeal setmeal = Setmeal.builder().id(setmealId).status(StatusConstant.DISABLE).build();
                    setmealMapper.update(setmeal);
                });
            }
        }
        //删除redis缓存中的菜单数据
        //TODO...
        return;
    }

    @Override
    public List<Dish> getDishByCondition(Long categoryId, String name) {
        Dish condition = Dish.builder().categoryId(categoryId == null ? null : categoryId)
                .name(name == null ? null : name)
                .build();
        List<Dish> dishList = dishMapper.dishsByCondition(condition);
        return dishList;

    }


    public List<DishVO> listDishsWithFlavors(Long categoryId) {
        String redisDishKey = "dish:cache" + categoryId;
        //1.先查询redis缓存，如果缓存中有数据，直接返回
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(redisDishKey);
        //2.如果缓存中没有数据，再查询数据库
        if (!CollectionUtils.isEmpty(dishVOList)) {
            log.info("查询redis缓存，命中数据直接返回.....");
            return dishVOList;
        }
        Dish condition = Dish.builder().categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        dishVOList = dishMapper.listDishsWithFlavors(condition);


        //3.把数据库查询的结果，加入缓存
        redisTemplate.opsForValue().set(redisDishKey, dishVOList);
        log.info("查询数据库数据，将查询到的数据缓存到redis数据库中");
        return dishVOList;
    }

}
