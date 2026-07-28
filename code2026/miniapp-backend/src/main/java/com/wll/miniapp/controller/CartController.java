// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入购物车实体类，映射数据库中的购物车表
import com.wll.common.entity.Cart;
// 导入购物车服务接口，提供购物车的增删改查操作
import com.wll.common.service.CartService;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入List集合类，用于存储查询到的购物车数据列表
import java.util.List;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /cart
@RequestMapping("/cart")
// 购物车控制器，处理购物车商品的添加、删除、修改、查询等请求
public class CartController {

    // 通过@Resource注解注入购物车服务实例（按名称装配）
    @Resource
    // 购物车服务接口引用，用于调用购物车相关业务逻辑
    private CartService cartService;

    // 映射POST请求到 /cart/add，向当前用户的购物车中添加商品
    @PostMapping("/add")
    // @RequestBody将请求体JSON绑定到Cart实体，HttpServletRequest获取当前用户ID
    public Result add(@RequestBody Cart cart, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID，设置为购物车所属用户
        cart.setUserId((Integer) request.getAttribute("userId"));
        // 调用购物车服务执行添加操作
        cartService.add(cart);
        // 返回添加成功的空结果
        return Result.success();
    }

    // 映射DELETE请求到 /cart/delete/{id}，根据ID删除购物车中的商品
    @DeleteMapping("/delete/{id}")
    // @PathVariable从URL路径中提取购物车记录ID
    public Result deleteById(@PathVariable Integer id) {
        // 调用购物车服务根据ID删除指定记录
        cartService.deleteById(id);
        // 返回删除成功的空结果
        return Result.success();
    }

    // 映射PUT请求到 /cart/update，更新购物车中商品的数量等信息
    @PutMapping("/update")
    // @RequestBody将请求体JSON绑定到Cart实体（包含要更新的ID和新数据）
    public Result updateById(@RequestBody Cart cart) {
        // 调用购物车服务根据实体中的ID更新记录
        cartService.updateById(cart);
        // 返回更新成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /cart/selectAll，查询当前用户购物车中的全部商品
    @GetMapping("/selectAll")
    // HttpServletRequest用于获取JWT拦截器设置的userId属性
    public Result selectAll(HttpServletRequest request) {
        // 创建购物车查询条件对象
        Cart cart = new Cart();
        // 设置查询条件：仅查询当前用户的购物车记录
        cart.setUserId((Integer) request.getAttribute("userId"));
        // 调用购物车服务按条件查询，返回当前用户的购物车商品列表
        List<Cart> list = cartService.selectAll(cart);
        // 将查询结果列表包装为成功结果返回
        return Result.success(list);
    }
}
