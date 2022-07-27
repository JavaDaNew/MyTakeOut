package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    //单独的Setmeal里没有套餐分类的名称 故新建Dto类加上套餐分类名称
    private String categoryName;
}
