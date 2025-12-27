package com.sky.controller.user;


import com.sky.entity.AddressBook;
import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.AddressService;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@Api(tags = "C端-地址簿")
@RestController()
@RequestMapping("/user/addressBook")
public class AddressController {


    @Autowired
    private AddressService addressService;

    /**
     * 新增收获地址
     * @param addressBook
     * @return
     */
    @ApiOperation("新增收货地址")
    @PostMapping
    public Result save(@RequestBody AddressBook addressBook){
        log.info("新增收货地址:{}",addressBook);
        addressService.save(addressBook);
        return Result.success();
    }

}