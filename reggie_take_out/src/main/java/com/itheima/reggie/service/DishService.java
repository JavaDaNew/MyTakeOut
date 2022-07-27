package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    /**
     * 新增套餐 同时插入菜品和对应的口味数据 dish、dish_flavor
     * @param dishDto
     */
    public void saveWithFlavor(DishDto dishDto);

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    public DishDto getByIdwithFlavor(Long id);

    /**
     * 修改菜品信息和口味信息
     * @param dishDto
     */
    public void updateWithFlavor(DishDto dishDto);
}
