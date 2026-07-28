// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入闲置商品实体类IdleGoods，对应数据库中的闲置商品表，包含名称、价格、描述、图片、状态、卖家ID等字段
import com.wll.common.entity.IdleGoods;
// 导入自定义异常类CustomException，用于业务异常处理
import com.wll.common.exception.CustomException;
// 导入闲置商品服务接口IdleGoodsService，封装闲置商品的发布、查询、下架、标记已售等业务逻辑
import com.wll.common.service.IdleGoodsService;
// 导入订单服务接口OrdersService，封装订单创建等业务逻辑（闲置商品购买时创建订单）
import com.wll.common.service.OrdersService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口
import java.util.List;

/**
 * 闲置商品控制器
 * 处理校园闲置物品交易的发布、管理、购买等功能
 * 所有写操作需要用户登录验证和卖家身份校验
 * 请求路径前缀：/idleGoods
 */
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/idleGoods前缀
@RequestMapping("/idleGoods")
// 声明IdleGoodsController公共类
public class IdleGoodsController {

    // @Resource注解：按名称注入IdleGoodsService Bean，负责闲置商品的核心业务逻辑
    @Resource
    private IdleGoodsService idleGoodsService;
    // @Resource注解：按名称注入OrdersService Bean，用于闲置商品购买时创建交易订单
    @Resource
    private OrdersService ordersService;

    /**
     * 发布闲置商品
     * 请求方式：POST /idleGoods/add
     * 需要登录，自动将当前登录用户设为卖家
     * @param idleGoods 闲置商品实体对象（JSON请求体），包含名称、价格、描述、图片等
     * @param session HTTP会话，用于获取当前登录用户
     * @return Result 操作成功响应，未登录时返回错误
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/idleGoods/add
    @PostMapping("/add")
    public Result add(@RequestBody IdleGoods idleGoods) {
        if (idleGoods.getSellerId() == null) return Result.error("请先登录");
        idleGoodsService.add(idleGoods);
        return Result.success();
    }

    /**
     * 删除闲置商品（仅卖家本人可删除）
     * 请求方式：DELETE /idleGoods/delete/{id}
     * @param id 闲置商品的主键ID（路径参数）
     * @param session HTTP会话，用于获取当前登录用户并验证权限
     * @return Result 操作成功或错误响应（未登录/商品不存在/无权操作）
     */
    // @DeleteMapping注解：HTTP DELETE映射，路径为/idleGoods/delete/{id}
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id, @RequestParam Integer userId) {
        if (userId == null) return Result.error("请先登录");
        IdleGoods goods = idleGoodsService.selectById(id);
        if (goods == null) return Result.error("商品不存在");
        if (!goods.getSellerId().equals(userId)) return Result.error("无权操作");
        idleGoodsService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改闲置商品信息（仅卖家本人可修改）
     * 请求方式：PUT /idleGoods/update
     * @param idleGoods 闲置商品实体对象（JSON请求体），包含要修改的字段和主键ID
     * @param session HTTP会话，用于获取当前登录用户并验证权限
     * @return Result 操作成功或错误响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/idleGoods/update
    @PutMapping("/update")
    public Result updateById(@RequestBody IdleGoods idleGoods, @RequestParam Integer userId) {
        if (userId == null) return Result.error("请先登录");
        IdleGoods exist = idleGoodsService.selectById(idleGoods.getId());
        if (exist == null) return Result.error("商品不存在");
        if (!exist.getSellerId().equals(userId)) return Result.error("无权操作");
        idleGoodsService.updateById(idleGoods);
        return Result.success();
    }

    /**
     * 根据ID查询单个闲置商品详情（无需登录）
     * 请求方式：GET /idleGoods/selectById/{id}
     * @param id 闲置商品的主键ID（路径参数）
     * @return Result 包含IdleGoods对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/idleGoods/selectById/{id}，公开接口无需登录
    @GetMapping("/selectById/{id}")
    // selectById方法：查询单个闲置商品的详细信息
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询闲置商品详情
        IdleGoods goods = idleGoodsService.selectById(id);
        // 返回商品详情数据
        return Result.success(goods);
    }

    /**
     * 查询所有闲置商品（可按分类、状态等条件过滤，无需登录）
     * 请求方式：GET /idleGoods/selectAll
     * @param idleGoods 可选的查询条件对象（通过URL参数传递字段值）
     * @return Result 包含IdleGoods列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/idleGoods/selectAll，公开接口
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的闲置商品
    public Result selectAll(IdleGoods idleGoods) {
        // 调用服务层查询闲置商品列表
        List<IdleGoods> list = idleGoodsService.selectAll(idleGoods);
        // 返回商品列表
        return Result.success(list);
    }

    /**
     * 分页查询闲置商品（无需登录）
     * 请求方式：GET /idleGoods/selectPage
     * @param idleGoods 可选的查询条件对象（通过URL参数传递字段值，如状态、分类）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/idleGoods/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询闲置商品
    public Result selectPage(IdleGoods idleGoods,
                             // @RequestParam：页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper自动追加LIMIT实现物理分页
        PageInfo<IdleGoods> page = idleGoodsService.selectPage(idleGoods, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

    /**
     * 查看我的闲置商品发布记录（需要登录）
     * 请求方式：GET /idleGoods/myListings?pageNum=1&pageSize=10
     * 只返回当前登录用户发布的闲置商品
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @param session HTTP会话，用于获取当前登录用户
     * @return Result 包含当前用户发布的闲置商品分页数据
     */
    // @GetMapping注解：HTTP GET映射，路径为/idleGoods/myListings，需要登录
    @GetMapping("/myListings")
    // myListings方法：查询当前登录用户发布的所有闲置商品
    public Result myListings(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam Integer userId) {
        if (userId == null) return Result.error("请先登录");
        IdleGoods query = new IdleGoods();
        query.setSellerId(userId);
        PageInfo<IdleGoods> page = idleGoodsService.selectPage(query, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 标记商品为已售出（仅卖家本人可操作）
     * 请求方式：PUT /idleGoods/sold/{id}
     * @param id 闲置商品的主键ID（路径参数）
     * @param session HTTP会话，用于获取当前登录用户并验证权限
     * @return Result 操作成功或错误响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/idleGoods/sold/{id}
    @PutMapping("/sold/{id}")
    public Result markAsSold(@PathVariable Integer id, @RequestParam Integer userId) {
        if (userId == null) return Result.error("请先登录");
        IdleGoods goods = idleGoodsService.selectById(id);
        if (goods == null) return Result.error("商品不存在");
        if (!goods.getSellerId().equals(userId)) return Result.error("无权操作");
        idleGoodsService.markAsSold(id);
        return Result.success();
    }

    /**
     * 下架闲置商品（仅卖家本人可操作）
     * 请求方式：PUT /idleGoods/takeDown/{id}
     * @param id 闲置商品的主键ID（路径参数）
     * @param session HTTP会话，用于获取当前登录用户并验证权限
     * @return Result 操作成功或错误响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/idleGoods/takeDown/{id}
    @PutMapping("/takeDown/{id}")
    public Result takeDown(@PathVariable Integer id, @RequestParam Integer userId) {
        if (userId == null) return Result.error("请先登录");
        IdleGoods goods = idleGoodsService.selectById(id);
        if (goods == null) return Result.error("商品不存在");
        if (!goods.getSellerId().equals(userId)) return Result.error("无权操作");
        idleGoodsService.takeDown(id);
        return Result.success();
    }

    /**
     * 购买闲置商品（需要登录，不能购买自己的商品）
     * 请求方式：POST /idleGoods/buy/{id}
     * 购买成功后自动创建订单并将商品标记为已售出
     * @param id 要购买的闲置商品ID（路径参数）
     * @param session HTTP会话，用于获取当前登录用户
     * @return Result 操作成功或错误响应（未登录/商品不可购买/不能买自己的）
     */
    // @PostMapping注解：HTTP POST映射，路径为/idleGoods/buy/{id}
    @PostMapping("/buy/{id}")
    public Result buy(@PathVariable Integer id, @RequestParam Integer userId) {
        if (userId == null) return Result.error("请先登录");
        IdleGoods goods = idleGoodsService.selectById(id);
        if (goods == null) return Result.error("商品不存在");
        if (!"在售".equals(goods.getStatus())) return Result.error("该商品已售出或已下架");
        if (goods.getSellerId().equals(userId)) return Result.error("不能购买自己的闲置商品");
        ordersService.addIdleOrder(userId, goods);
        idleGoodsService.markAsSold(id);
        return Result.success();
    }
}
