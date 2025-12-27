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

    @ApiOperation("查询当前用户的地址列表")
    @GetMapping("/list")
    public Result<List<AddressBook>> list(){
        log.info("查询当前用户的地址列表");
        List<AddressBook> addressBookList = addressService.list();
        return Result.success(addressBookList);
    }

    @ApiOperation("设置默认的收货地址")
    @PutMapping("/default")
    public Result setDefaultAddress(@RequestBody AddressBook addressBook){
        log.info("设置id：{}为什么默认地址",addressBook.getId());
        addressService.setDefaultAddress(addressBook.getId());
        return Result.success();
    }


    @ApiOperation("查询默认的收货地址")
    @GetMapping("/default")
    public Result<AddressBook> getdefaultAddress(){
        log.info("得到1默认收获地址");
        AddressBook addressBook = addressService.getdefaultAddress();
        return Result.success(addressBook);
    }

    @ApiOperation("根据ID查询收货地址信息")
    @GetMapping("/{id}")
    public Result<AddressBook> getAddressById(@PathVariable Long id){
        log.info("根据ID:{}查询收货地址信息", id);
        AddressBook addressBook = addressService.getInfoById(id);
        return Result.success(addressBook);
    }

    @ApiOperation("根据id修改地址")
    @PutMapping()
    public Result updateById(@RequestBody AddressBook addressBook){
        log.info("根据id：{}修改地址信息", addressBook.getId());
        addressService.update(addressBook);
        return Result.success();
    }


}