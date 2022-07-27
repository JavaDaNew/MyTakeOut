package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<DishFlavor>();

    //单独的Dish里没有菜品分类的名称 故新建Dto类加上菜品分类名称
    private String categoryName;

    private Integer copies;
}
