package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.utils.AliOssUtil;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 公共controller
 */
@Slf4j
@Api(tags = "分类管理")
@RestController
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation("分页")
    @GetMapping("/page")
    public Result page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("条件分页查询:{}",categoryPageQueryDTO);
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    //根据id查询分类
    @ApiOperation("根据id查询分类")
    @GetMapping("/{id}")
    public Result getDishById(@PathVariable Long id){
        log.info("查询id：{}的分类",id);
        Category category = categoryService.getCategoryById(id);
        return Result.success(category);
    }


    @ApiOperation("修改分类")
    @PutMapping
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("修改分类:{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @PutMapping("/status/{status}/{id}")
    @ApiOperation("启用、禁用分类数据信息")
    public Result updateStatus(@PathVariable Integer status, @PathVariable Long id) {
        log.info("修改分类状态：id={}, status={}", id, status);
        categoryService.updateStatus(id, status);
        return Result.success();
    }

    @ApiOperation("新增分类")
    @PostMapping
    public Result save(@RequestBody CategoryDTO categoryDTO){
        log.info("新增分类数据:{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @ApiOperation("根据id删除分类")
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable Long id){
        log.info("根据id：{}删除分类",id);
        categoryService.deleteById(id);
        return Result.success();
    }


    @ApiOperation("根据type类型查询")
    @GetMapping("/list")
    public Result getByType(@RequestParam(required = false) Integer type){
        log.info("根据type：{}查询分类", type);
        List<Category> categoryList = categoryService.getByType(type);
        return Result.success(categoryList);
    }

}