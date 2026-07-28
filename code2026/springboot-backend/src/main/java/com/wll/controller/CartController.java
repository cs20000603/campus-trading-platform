// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入购物车实体类Cart，对应数据库中的购物车表，包含用户ID、商品ID、数量等字段
import com.wll.common.entity.Cart;
// 导入购物车服务接口CartService，封装购物车的增删改查业务逻辑
import com.wll.common.service.CartService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于接收和返回列表类型数据
import java.util.List;

/**
 * 购物车控制器
 * 处理购物车相关的增删改查操作，包括添加商品到购物车、删除购物车项、修改数量、查询等
 * 请求路径前缀：/cart
 **/
// @RestController注解：标记该类为RESTful控制器，所有方法返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/cart前缀
@RequestMapping("/cart")
// 声明CartController公共类
public class CartController {

    // @Resource注解：按名称注入CartService Bean，cartService负责购物车数据的增删改查业务逻辑
    @Resource
    private CartService cartService;

    /**
     * 新增购物车记录
     * 请求方式：POST /cart/add
     * @param cart 购物车实体对象（JSON请求体），包含用户ID、商品ID、数量等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/cart/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Cart对象，将商品添加到用户的购物车
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为Cart Java对象
    public Result add(@RequestBody Cart cart) {
        // 调用服务层将购物车记录插入数据库
        cartService.add(cart);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID删除购物车记录
     * 请求方式：DELETE /cart/delete/{id}
     * @param id 购物车记录的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，请求路径为/cart/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据路径变量id删除购物车记录
    // @PathVariable注解：绑定URL中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层根据主键ID从数据库删除购物车记录
        cartService.deleteById(id);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 修改购物车记录（如修改商品数量）
     * 请求方式：PUT /cart/update
     * @param cart 购物车实体对象（JSON请求体），包含要修改的字段及主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，请求路径为/cart/update
    @PutMapping("/update")
    // updateById方法：根据主键更新购物车记录（如修改商品数量）
    public Result updateById(@RequestBody Cart cart) {
        // 调用服务层根据主键ID更新购物车数据库记录
        cartService.updateById(cart);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单条购物车记录
     * 请求方式：GET /cart/selectById/{id}
     * @param id 购物车记录的主键ID（路径参数）
     * @return Result 包含Cart对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/cart/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单条购物车记录详情
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询单条购物车记录
        Cart cart = cartService.selectById(id);
        // 返回包含购物车数据的成功响应
        return Result.success(cart);
    }

    /**
     * 查询所有购物车记录（可按用户ID或商品ID等条件过滤）
     * 请求方式：GET /cart/selectAll
     * @param cart 可选的查询条件对象（通过URL参数传递字段值，如userId=1）
     * @return Result 包含Cart列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/cart/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的购物车记录
    public Result selectAll(Cart cart) {
        // 调用服务层查询所有符合条件的购物车记录
        List<Cart> list = cartService.selectAll(cart);
        // 返回包含购物车列表的成功响应
        return Result.success(list);
    }

    /**
     * 分页查询购物车记录
     * 请求方式：GET /cart/selectPage
     * @param cart 可选的查询条件对象（通过URL参数传递字段值）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据（数据列表、总条数、总页数等）的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/cart/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询购物车记录
    public Result selectPage(Cart cart,
                             // @RequestParam：从URL查询参数获取pageNum，默认为1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：从URL查询参数获取pageSize，默认为10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<Cart> page = cartService.selectPage(cart, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

}
