// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入订单实体类，映射数据库中的订单表
import com.wll.common.entity.Orders;
// 导入店铺实体类，映射数据库中的店铺表
import com.wll.common.entity.Shop;
// 导入店铺数据访问接口，用于查询当前用户拥有的店铺
import com.wll.common.mapper.ShopMapper;
// 导入订单服务接口，提供订单的增删改查等操作
import com.wll.common.service.OrdersService;
// 导入PageHelper分页插件返回的分页信息类
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /orders
@RequestMapping("/orders")
// 订单控制器，处理用户订单的创建、查询、更新以及商家订单管理等请求
public class OrdersController {

    // 通过@Resource注解注入订单服务实例（按名称装配）
    @Resource
    // 订单服务接口引用，用于调用订单相关业务逻辑
    private OrdersService ordersService;
    // 通过@Resource注解注入店铺数据访问实例（按名称装配）
    @Resource
    // 店铺数据访问接口引用，用于查询用户拥有的店铺信息
    private ShopMapper shopMapper;

    // 映射POST请求到 /orders/add，创建新订单（用户下单）
    @PostMapping("/add")
    // @RequestBody将请求体JSON绑定到Orders实体，HttpServletRequest获取当前用户ID
    public Result add(@RequestBody Orders orders, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID，设置为订单所属用户
        orders.setUserId((Integer) request.getAttribute("userId"));
        // 调用订单服务执行创建订单操作
        ordersService.add(orders);
        // 返回创建成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /orders/selectById/{id}，根据订单ID查询订单详情
    @GetMapping("/selectById/{id}")
    // @PathVariable从URL路径中提取订单ID
    public Result selectById(@PathVariable Integer id) {
        // 调用订单服务根据ID查询单个订单的详细信息
        Orders orders = ordersService.selectById(id);
        // 将查询到的订单详情包装为成功结果返回
        return Result.success(orders);
    }

    // 映射GET请求到 /orders/selectPage，分页查询当前用户的订单
    @GetMapping("/selectPage")
    // @RequestParam获取页码参数，默认第1页
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            // @RequestParam获取每页条数参数，默认每页10条
            @RequestParam(defaultValue = "10") Integer pageSize,
            // HttpServletRequest获取当前用户ID
            HttpServletRequest request) {
        // 创建订单查询条件对象
        Orders orders = new Orders();
        // 设置查询条件：仅查询当前用户的订单
        orders.setUserId((Integer) request.getAttribute("userId"));
        // 调用订单服务进行分页查询，返回当前用户的订单列表
        PageInfo<Orders> page = ordersService.selectPage(orders, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }

    // 映射PUT请求到 /orders/update，更新订单信息（如修改状态、备注等）
    @PutMapping("/update")
    // @RequestBody将请求体JSON绑定到Orders实体（含要更新的ID和新数据）
    public Result updateById(@RequestBody Orders orders) {
        // 调用订单服务根据实体中的ID更新订单记录
        ordersService.updateById(orders);
        // 返回更新成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /orders/shopOrders，分页查询商家店铺的订单（商家视角）
    @GetMapping("/shopOrders")
    // @RequestParam获取订单状态筛选参数，required=false表示可选
    public Result shopOrders(
            @RequestParam(required = false) String status,
            // @RequestParam获取页码参数，默认第1页
            @RequestParam(defaultValue = "1") Integer pageNum,
            // @RequestParam获取每页条数参数，默认每页10条
            @RequestParam(defaultValue = "10") Integer pageSize,
            // HttpServletRequest获取当前用户ID
            HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据用户ID（店铺所有者）查询该用户拥有的店铺
        Shop shop = shopMapper.selectByOwnerId(userId);
        // 判断用户是否拥有店铺
        if (shop == null) {
            // 如果没有店铺，返回错误提示
            return Result.error("您还没有店铺");
        }
        // 调用订单服务按店铺ID和状态进行分页查询，返回该店铺的订单列表
        PageInfo<Orders> page = ordersService.selectPageByShopId(shop.getId(), status, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }
}
