package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐 同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息 操作setmeal 执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联信息 操作setmeal_dish 执行insert
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐及套餐关联的菜品
     * @param ids
     */
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (1,2,3) and status = 1;
        //查询套餐状态 确定是否可以删除
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();

        lqw.in(Setmeal::getId,ids);
        lqw.eq(Setmeal::getStatus,1);

        int count = this.count(lqw);
        //如果不能删除 抛出一个业务异常
        if(count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }

        //如果可以删除 先删除套餐表中的数据 -- setmeal
        this.removeByIds(ids);

        //delete from setmeal_dish where setmeal_id in (1,2,3)
        //删除关系中的数据 -- setmeal_dish
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);

        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    public SetmealDto getByIdwithDish(Long id) {
        //查询当前套餐基本信息 setmeal属性
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询当前套餐的关联菜品信息 setmeal_dish表查询
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmeal.getId());
        List<SetmealDish> list = setmealDishService.list(lqw);
        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    /**
     * 修改套餐信息和关联菜品信息
     * @param setmealDto
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //根据setmealDto表的当前信息进行更新
        this.updateById(setmealDto);

        //清理当前关联的菜品数据 -- setmeal_dish 表的delete操作
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId,setmealDto.getId());

        setmealDishService.remove(lqw); //删除

        //添加当前提交过来的菜品数据 -- setmeal_dish 表的insert操作
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();

        setmealDishes.stream().map((item) ->{
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(setmealDishes); //新增
    }
}
