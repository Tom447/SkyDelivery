package com.sky.controller.user;



import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@Api(tags = "C端-分类")
@RestController("UserCategoryController")
@RequestMapping("/user/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation("根据type类型查询")
    @GetMapping("/list")
    public Result getByType(@RequestParam(required = false) Integer type){
        log.info("C端根据type：{}查询分类", type);
        List<Category> categoryList = categoryService.getByType(type);
        return Result.success(categoryList);
    }



}