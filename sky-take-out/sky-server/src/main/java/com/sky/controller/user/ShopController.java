package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@Api(tags = "C端-店铺操作接口")
@Slf4j
@RestController("userShopController")
@RequestMapping("/user/shop")
public class ShopController {

    private static final String SHOP_STATUS_KEY = "shop_status";
    @Autowired
    private RedisTemplate redisTemplate;
    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result getStatus(){
        Integer status = (Integer)redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        log.info("redis中查询店铺的营业状态.营业状态为:{}", status == 1 ? "营业中":"打样中");
        return Result.success(status);
    }


}
