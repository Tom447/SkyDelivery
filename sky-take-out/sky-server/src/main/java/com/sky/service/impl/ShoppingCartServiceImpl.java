package com.sky.service.impl;


import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.utils.BeanHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    //添加购物车
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //1.查询当前的购物车中是否含有指定的商品（菜品-口味，套餐）
        ShoppingCart shoppingCart = BeanHelper.copyProperties(shoppingCartDTO, ShoppingCart.class);
        shoppingCart.setUserId(BaseContext.getCurrentId());


        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        //2.如果该商品在当前用户的购物车中已经存在，则数量+1

        if (!CollectionUtils.isEmpty(shoppingCartList)) {
            ShoppingCart cart = shoppingCartList.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
        } else {
            //3.如果该商品在当前的用户的购物车中不存在，则添加一条新的购物车数据
            Long dishId = shoppingCart.getDishId();

            if (dishId != null){//菜品
                List<Dish> dishList = dishMapper.getDishsByIds(Arrays.asList(dishId));
                Dish dish = dishList.get(0);
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setName(dish.getName());
            }else{//套餐
                Long setmealId = shoppingCart.getSetmealId();
                Setmeal setmeal = setmealMapper.getSetmealById(setmealId);
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setName(setmeal.getName());
            }

            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);

            //插入数据
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public List<ShoppingCart> list() {
        ShoppingCart condition = ShoppingCart.builder().userId(BaseContext.getCurrentId()).build();
       return shoppingCartMapper.list(condition);
    }
}
