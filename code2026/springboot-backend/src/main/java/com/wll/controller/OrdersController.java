// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入订单实体类Orders，对应数据库中的订单表，包含订单号、用户ID、商品信息、总金额、订单状态、时间等字段
import com.wll.common.entity.Orders;
// 导入订单服务接口OrdersService，封装订单的增删改查及按店铺查询等业务逻辑
import com.wll.common.service.OrdersService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回订单列表
import java.util.List;

/**
 * 订单控制器
 * 处理订单的增删改查操作，支持商家按店铺和状态查询订单
 * 请求路径前缀：/orders
 **/
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/orders前缀
@RequestMapping("/orders")
// 声明OrdersController公共类
public class OrdersController {

    // @Resource注解：按名称注入OrdersService Bean，ordersService负责订单的增删改查及按店铺查询业务逻辑
    @Resource
    private OrdersService ordersService;

    /**
     * 新增订单（用户下单）
     * 请求方式：POST /orders/add
     * @param orders 订单实体对象（JSON请求体），包含用户ID、商品信息、总金额、收货地址等
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/orders/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Orders对象，创建新订单
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为Orders对象
    public Result add(@RequestBody Orders orders) {
        // 调用服务层创建订单（包含生成订单号、计算总金额、扣减库存等业务逻辑）
        ordersService.add(orders);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 根据ID删除订单
     * 请求方式：DELETE /orders/delete/{id}
     * @param id 订单的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，路径为/orders/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据主键ID删除订单
    // @PathVariable注解：绑定URL中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层从数据库删除订单记录
        ordersService.deleteById(id);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 修改订单信息（如更新订单状态、物流信息等）
     * 请求方式：PUT /orders/update
     * @param orders 订单实体对象（JSON请求体），包含要修改的字段和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/orders/update
    @PutMapping("/update")
    // updateById方法：根据主键更新订单信息（常用于修改订单状态：待发货→已发货→已完成）
    public Result updateById(@RequestBody Orders orders) {
        // 调用服务层根据主键ID更新订单数据库记录
        ordersService.updateById(orders);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单个订单详情
     * 请求方式：GET /orders/selectById/{id}
     * @param id 订单的主键ID（路径参数）
     * @return Result 包含Orders对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/orders/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：查询单个订单的详细信息
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询订单详情
        Orders orders = ordersService.selectById(id);
        // 返回订单数据
        return Result.success(orders);
    }

    /**
     * 查询所有订单（可按用户ID等条件过滤）
     * 请求方式：GET /orders/selectAll
     * @param orders 可选的查询条件对象（通过URL参数传递字段值，如userId=1）
     * @return Result 包含Orders列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/orders/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的订单
    public Result selectAll(Orders orders) {
        // 调用服务层查询订单列表
        List<Orders> list = ordersService.selectAll(orders);
        // 返回订单列表
        return Result.success(list);
    }

    /**
     * 分页查询订单列表
     * 请求方式：GET /orders/selectPage
     * @param orders 可选的查询条件对象（通过URL参数传递字段值，如用户ID、订单状态）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/orders/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询订单列表
    public Result selectPage(Orders orders,
                             // @RequestParam：当前页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<Orders> page = ordersService.selectPage(orders, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

    /**
     * 商家查询自己店铺的订单（支持按订单状态筛选）
     * 请求方式：GET /orders/shopOrders?shopId=xxx&status=xxx&pageNum=1&pageSize=10
     * @param shopId 店铺ID（必填），用于筛选属于该店铺的订单
     * @param status 订单状态（可选），如"待发货"、"已发货"、"已完成"等，不传则查询所有状态
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含该店铺订单的PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/orders/shopOrders
    @GetMapping("/shopOrders")
    // shopOrders方法：商家查看自己店铺的订单，支持按状态筛选
    public Result shopOrders(@RequestParam Integer shopId,                // 店铺ID，必填
                             @RequestParam(required = false) String status,  // 订单状态，可选（如"待发货"）
                             @RequestParam(defaultValue = "1") Integer pageNum,   // 页码，默认1
                             @RequestParam(defaultValue = "10") Integer pageSize  // 每页条数，默认10
                             ) {
        // 调用服务层按店铺ID和订单状态分页查询，商家可以切换状态Tab查看不同状态的订单
        PageInfo<Orders> page = ordersService.selectPageByShopId(shopId, status, pageNum, pageSize);
        // 返回该店铺的订单分页数据
        return Result.success(page);
    }

}
