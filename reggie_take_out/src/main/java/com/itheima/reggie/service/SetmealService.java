package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐 同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐及套餐关联的菜品
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    public SetmealDto getByIdwithDish(Long id);

    /**
     * 修改套餐信息和关联菜品信息
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);
}
