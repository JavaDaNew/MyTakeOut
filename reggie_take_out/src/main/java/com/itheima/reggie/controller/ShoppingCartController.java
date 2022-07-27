package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        log.info("购物车数据：{}",shoppingCart);

        //设置用户id 指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或套餐 是否在购物车中
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        //添加条件查询
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,currentId);

        if(dishId != null){
            //添加到购物车的是菜品
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            lqw.eq(ShoppingCart::getSetmealId,setmealId);
        }

        //SQL: select * from shopping_cart where user_id ? and dish_id/setmeal_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);

        if(cartServiceOne != null ){
            /*//判断菜品的口味是否一样
            String dishFlavor = cartServiceOne.getDishFlavor();
            if(!dishFlavor.equals(shoppingCart.getDishFlavor())){
                //菜品口味不一样 新添加到购物车
                LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(ShoppingCart::getDishFlavor,dishFlavor);
                ShoppingCart cartServiceOne1 = shoppingCartService.getOne(queryWrapper);
                shoppingCartService.save(cartServiceOne1);
            }*/

            //菜品口味一样
            //如果菜品在购物车中已经存在 就在原来数量基础上加一
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number + 1);
            shoppingCartService.updateById(cartServiceOne);
        }else {
            //如果菜品在购物车中不存在 则添加到购物车 数量默认是一
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            System.out.println(LocalDateTime.now());

            shoppingCartService.save(shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车 ...");

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        //让晚添加的排序在最下面
        lqw.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(lqw);

        return R.success(list);
    }

    /**
     * 购物车中减少
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        Long setmealId = shoppingCart.getSetmealId();
        Long dishId = shoppingCart.getDishId();

        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());

        if(dishId != null){
            //菜品
            lqw.eq(ShoppingCart::getDishId,dishId);
        }else {
            //套餐
            lqw.eq(ShoppingCart::getSetmealId,setmealId);
        }
        //得到当前用户购物车中的数据
        ShoppingCart cartServiceOne = shoppingCartService.getOne(lqw);

        Integer number = cartServiceOne.getNumber();

        if(number == 1){
            //如果菜品/套餐数量为1 则移除
            shoppingCartService.remove(lqw);
        }else {
            //如果菜品/套餐数量为多份
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
        }
        //最后返回购物车中修改后的数据
        return R.success(cartServiceOne);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //SQL: delete from shopping_cart where user_id = ?
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功");
    }

}
