package com.sky.controller;

import com.sky.result.Result;
import com.sky.service.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "店铺")
@RestController
@RequestMapping("/admin/shop")
public class ShopController {

    @Autowired
    private ShopService shopService;

    // ✅ 获取营业状态：GET /admin/shop/status
    @ApiOperation("获取店铺的营业状态")
    @GetMapping("/status")
    public Result<Integer> getShopStatus() {
        log.info("获取店铺的营业状态");
        Integer status = shopService.getShopStatus();
        return Result.success(status);
    }

    // ✅ 设置营业状态：PUT /admin/shop/status/1
    @ApiOperation("设置店铺的营业状态")
    @PutMapping("/{status}")
    public Result setShopStatus(@PathVariable Integer status) {
        log.info("设置店铺的营业状态: {}", status);
        shopService.setShopStatus(status);
        return Result.success();
    }
}