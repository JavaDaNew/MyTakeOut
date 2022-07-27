package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据:{}" ,orders);

        orderService.submit(orders);

        return R.success("下单成功");
    }

    /**
     * 分页查看下单后的订单列表
     * @param page
     * @param pageSize
     * @return
     */
    @Transactional
    @GetMapping("/userPage")
        public R<Page> userPage(int page, int pageSize){ // 1, 5
        log.info("查看订单 ... ");

        //分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        Page<OrdersDto> ordersDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();

        //排序条件
        lqw.orderByDesc(Orders::getOrderTime);

        orderService.page(pageInfo,lqw);

        log.info("pageInfo:{}",pageInfo); // pageInfo current = 1 , size = 5 ,订单总数 total = ? ,订单详细信息 records(Orders) :

        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> records = pageInfo.getRecords(); //把pageInfo中的订单详细信息通过stream流的方式传递

        List<OrdersDto> list = records.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();

            BeanUtils.copyProperties(item,ordersDto);
            Long id = item.getId();
            //根据id查 分类对象
            Orders orders = orderService.getById(id);
            String number = orders.getNumber();

            LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderDetail::getOrderId,number);
            List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

            //订单数量
            int num = 0 ;

            for (OrderDetail orderDetail:orderDetailList){
                num += orderDetail.getNumber().intValue();
            }

            ordersDto.setSumNum(num);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);
    }

    /**
     * 再来一单
     * @param order1
     * @return
     */
    //再来一单
    /*@Transactional
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders order1){
        //取得orderId
        Long id = order1.getId();
        Orders orders = orderService.getById(id);
        //设置订单号码
        long orderId = IdWorker.getId();
        orders.setId(orderId);
        //设置订单号码
        String number = String.valueOf(IdWorker.getId());
        orders.setNumber(number);
        //设置下单时间
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        //向订单表中插入一条数据
        orderService.save(orders);
        //修改订单明细表
        LambdaQueryWrapper<OrderDetail> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> list = orderDetailService.list(queryWrapper);
        list.stream().map((item)->{
            //订单明细表id
            long detailId = IdWorker.getId();
            //设置订单号码
            item.setOrderId(orderId);
            item.setId(detailId);
            return item;
        }).collect(Collectors.toList());

        //向订单明细表中插入多条数据
        orderDetailService.saveBatch(list);
        return R.success("再来一单");
    }*/

    /**
     * 管理端订单明细
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number,String beginTime,String endTime){
        //构造分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        Page<OrdersDto> ordersDtoPage=new Page<>();
        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //根据number进行模糊查询
        queryWrapper.like(!StringUtils.isEmpty(number),Orders::getNumber,number);
        //根据Datetime进行时间范围查询

//        log.info("开始时间：{}",beginTime);
//        log.info("结束时间：{}",endTime);
        //查询时间界限
        if(beginTime!=null&&endTime!=null){
            queryWrapper.ge(Orders::getOrderTime,beginTime);
            queryWrapper.le(Orders::getOrderTime,endTime);
        }
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //进行分页查询
        orderService.page(pageInfo,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo,ordersDtoPage,"records");

        List<Orders> records=pageInfo.getRecords();

        List<OrdersDto> list=records.stream().map((item)->{
            OrdersDto ordersDto=new OrdersDto();

            BeanUtils.copyProperties(item,ordersDto);
            String name="用户"+item.getUserId();
            ordersDto.setUserName(name);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);
        return R.success(ordersDtoPage);
    }

    /**
     * 外卖订单派送
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> send(@RequestBody Orders orders){
        Long id = orders.getId();
        Integer status = orders.getStatus();
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getId,id);
        Orders one = orderService.getOne(queryWrapper);
        one.setStatus(status);
        orderService.updateById(one);
        return R.success("派送成功");
    }

}
