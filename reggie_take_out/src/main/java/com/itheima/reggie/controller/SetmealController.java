package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;

import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息：{}",setmealDto);

        setmealService.saveWithDish(setmealDto);

        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize , String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        Page<SetmealDto> setmealDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();

        //添加过滤条件
        lqw.like(StringUtils.isNotEmpty(name), Setmeal::getName,name);
        //添加排序条件
        lqw.orderByDesc(Setmeal::getUpdateTime);

        //进行分页查询
        setmealService.page(pageInfo,lqw);

        //对象拷贝 泛型不一样 所以忽略元素信息 先只拷贝page和pageSize属性
        BeanUtils.copyProperties(pageInfo,setmealDtoPage,"records");

        //通过getRecords方法得到pageInfo中的Setmean元素信息
        List<Setmeal> records = pageInfo.getRecords();

        //通过stream流的方式拷贝到setmealDtoPage中
        List<SetmealDto> list = records.stream().map((item) ->{

            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝数据
            BeanUtils.copyProperties(item,setmealDto);

            Long categoryId = item.getCategoryId(); // 分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        //给setmealDtoPage set数据 数据是从stream流的方式从pageInfo里得到的records中拷贝出来的
        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        setmealService.removeWithDish(ids);

        return R.success("删除成功");
    }

    /**
     * 批量起售、停售
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> sale(@PathVariable int status ,String[] ids){
        for (String id : ids) {
            Setmeal setmeal = setmealService.getById(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }


    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> get(@PathVariable Long id){
        log.info("根据id查询套餐信息和对应的套餐菜品信息 ... ");

        SetmealDto setmealDto = setmealService.getByIdwithDish(id);

        return R.success(setmealDto);
    }

    /**
     * 修改菜品
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());

        setmealService.updateWithDish(setmealDto);

        return R.success("修改菜品成功");
    }


    /**
     * 套餐在移动端显示
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //构造查询条件
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(setmeal.getCategoryId() != null ,Setmeal::getCategoryId,setmeal.getCategoryId());
        //添加条件 查询状态为1(启售)
        lqw.eq(Setmeal::getStatus,1);
        lqw.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(lqw);

        return R.success(setmealList);
    }

}
