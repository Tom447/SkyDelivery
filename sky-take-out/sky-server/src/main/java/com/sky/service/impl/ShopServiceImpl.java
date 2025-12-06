package com.sky.service.impl;


import com.sky.exception.BusinessException;
import com.sky.service.ShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ShopServiceImpl implements ShopService {
    @Autowired
    private RedisTemplate redisTemplate;

    private static final String SHOP_STATUS_KEY = "shop_status";
    @Override
    public Integer getShopStatus() {
        // 从 Redis 获取状态
        Object statusObj = redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        if (statusObj == null) {
            return 0; // 默认打烊
        }
        return (Integer) statusObj;
    }

    @Override
    public void setShopStatus(Integer status) {
        // 校验状态合法性
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值不合法，只能是 0 或 1");
        }
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY, status, 24, TimeUnit.HOURS);
        log.info("店铺营业状态已更新为：{}", status);
    }
}
