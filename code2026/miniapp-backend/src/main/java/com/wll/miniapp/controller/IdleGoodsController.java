// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入闲置商品实体类，映射数据库中的闲置商品表
import com.wll.common.entity.IdleGoods;
// 导入闲置商品服务接口，提供闲置商品的增删改查等操作
import com.wll.common.service.IdleGoodsService;
// 导入订单服务接口，用于闲置商品购买时创建订单
import com.wll.common.service.OrdersService;
// 导入PageHelper分页插件返回的分页信息类
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入List集合类，用于存储查询到的闲置商品数据列表
import java.util.List;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /idleGoods
@RequestMapping("/idleGoods")
// 闲置商品控制器，处理校园闲置交易中商品的发布、删除、修改、查询、购买等请求
public class IdleGoodsController {

    // 通过@Resource注解注入闲置商品服务实例（按名称装配）
    @Resource
    // 闲置商品服务接口引用，用于调用闲置商品相关业务逻辑
    private IdleGoodsService idleGoodsService;
    // 通过@Resource注解注入订单服务实例（按名称装配）
    @Resource
    // 订单服务接口引用，用于闲置商品购买时生成订单
    private OrdersService ordersService;

    // 映射POST请求到 /idleGoods/add，发布新的闲置商品
    @PostMapping("/add")
    // @RequestBody将请求体JSON绑定到IdleGoods实体，HttpServletRequest获取当前用户ID
    public Result add(@RequestBody IdleGoods idleGoods, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 设置闲置商品的卖家ID为当前登录用户
        idleGoods.setSellerId(userId);
        // 调用闲置商品服务执行发布操作
        idleGoodsService.add(idleGoods);
        // 返回发布成功的空结果
        return Result.success();
    }

    // 映射DELETE请求到 /idleGoods/delete/{id}，删除自己发布的闲置商品
    @DeleteMapping("/delete/{id}")
    // @PathVariable从URL路径中提取闲置商品ID，HttpServletRequest获取当前用户ID用于权限校验
    public Result deleteById(@PathVariable Integer id, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据ID查询要删除的闲置商品信息
        IdleGoods goods = idleGoodsService.selectById(id);
        // 校验商品是否存在，不存在则返回错误
        if (goods == null) return Result.error("商品不存在");
        // 校验当前用户是否为该商品的卖家（仅卖家有权删除），非法操作则返回错误
        if (!goods.getSellerId().equals(userId)) return Result.error("无权操作");
        // 权限校验通过，执行删除操作
        idleGoodsService.deleteById(id);
        // 返回删除成功的空结果
        return Result.success();
    }

    // 映射PUT请求到 /idleGoods/update，修改自己发布的闲置商品信息
    @PutMapping("/update")
    // @RequestBody将请求体JSON绑定到IdleGoods实体（含要修改的ID和新数据），HttpServletRequest获取当前用户ID
    public Result updateById(@RequestBody IdleGoods idleGoods, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据实体中的ID查询数据库中已存在的商品记录
        IdleGoods exist = idleGoodsService.selectById(idleGoods.getId());
        // 校验商品是否存在，不存在则返回错误
        if (exist == null) return Result.error("商品不存在");
        // 校验当前用户是否为该商品的卖家（仅卖家有权修改），非法操作则返回错误
        if (!exist.getSellerId().equals(userId)) return Result.error("无权操作");
        // 权限校验通过，执行更新操作
        idleGoodsService.updateById(idleGoods);
        // 返回更新成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /idleGoods/selectById/{id}，根据ID查询闲置商品详情
    @GetMapping("/selectById/{id}")
    // @PathVariable从URL路径中提取闲置商品ID
    public Result selectById(@PathVariable Integer id) {
        // 调用闲置商品服务根据ID查询单个商品的详细信息
        IdleGoods goods = idleGoodsService.selectById(id);
        // 将查询到的商品详情包装为成功结果返回
        return Result.success(goods);
    }

    // 映射GET请求到 /idleGoods/selectAll，根据条件查询全部闲置商品
    @GetMapping("/selectAll")
    // IdleGoods实体作为查询条件接收参数（如status按状态筛选、keyword模糊搜索等）
    public Result selectAll(IdleGoods idleGoods) {
        // 调用闲置商品服务按条件查询全部匹配的商品列表
        List<IdleGoods> list = idleGoodsService.selectAll(idleGoods);
        // 将查询到的商品列表包装为成功结果返回
        return Result.success(list);
    }

    // 映射GET请求到 /idleGoods/selectPage，分页查询闲置商品
    @GetMapping("/selectPage")
    // IdleGoods实体作为查询条件接收参数
    public Result selectPage(IdleGoods idleGoods,
                             // @RequestParam获取页码参数，默认第1页
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam获取每页条数参数，默认每页10条
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用闲置商品服务进行分页查询
        PageInfo<IdleGoods> page = idleGoodsService.selectPage(idleGoods, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }

    // 映射GET请求到 /idleGoods/myListings，分页查询当前用户发布的闲置商品
    @GetMapping("/myListings")
    // @RequestParam获取页码参数，默认第1页
    public Result myListings(@RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam获取每页条数参数，默认每页10条
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             // HttpServletRequest获取当前用户ID
                             HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 创建闲置商品查询条件对象
        IdleGoods query = new IdleGoods();
        // 设置查询条件：仅查询当前用户作为卖家发布的商品
        query.setSellerId(userId);
        // 调用闲置商品服务进行分页查询，返回当前用户的发布列表
        PageInfo<IdleGoods> page = idleGoodsService.selectPage(query, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }

    // 映射PUT请求到 /idleGoods/sold/{id}，卖家手动标记商品为已售出状态
    @PutMapping("/sold/{id}")
    // @PathVariable从URL路径中提取闲置商品ID，HttpServletRequest获取当前用户ID用于权限校验
    public Result markAsSold(@PathVariable Integer id, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据ID查询要标记的闲置商品信息
        IdleGoods goods = idleGoodsService.selectById(id);
        // 校验商品是否存在
        if (goods == null) return Result.error("商品不存在");
        // 校验当前用户是否为该商品的卖家
        if (!goods.getSellerId().equals(userId)) return Result.error("无权操作");
        // 权限校验通过，调用服务将商品状态标记为已售出
        idleGoodsService.markAsSold(id);
        // 返回操作成功的空结果
        return Result.success();
    }

    // 映射PUT请求到 /idleGoods/takeDown/{id}，卖家下架自己发布的闲置商品
    @PutMapping("/takeDown/{id}")
    // @PathVariable从URL路径中提取闲置商品ID，HttpServletRequest获取当前用户ID用于权限校验
    public Result takeDown(@PathVariable Integer id, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据ID查询要下架的闲置商品信息
        IdleGoods goods = idleGoodsService.selectById(id);
        // 校验商品是否存在
        if (goods == null) return Result.error("商品不存在");
        // 校验当前用户是否为该商品的卖家
        if (!goods.getSellerId().equals(userId)) return Result.error("无权操作");
        // 权限校验通过，调用服务将商品状态下架
        idleGoodsService.takeDown(id);
        // 返回操作成功的空结果
        return Result.success();
    }

    // 映射POST请求到 /idleGoods/buy/{id}，用户购买指定的闲置商品
    @PostMapping("/buy/{id}")
    // @PathVariable从URL路径中提取要购买的闲置商品ID，HttpServletRequest获取当前用户ID
    public Result buy(@PathVariable Integer id, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID（即买家ID）
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据ID查询要购买的闲置商品信息
        IdleGoods goods = idleGoodsService.selectById(id);
        // 校验商品是否存在
        if (goods == null) return Result.error("商品不存在");
        // 校验商品状态是否为"在售"，已售出或已下架的商品不能购买
        if (!"在售".equals(goods.getStatus())) return Result.error("该商品已售出或已下架");
        // 校验买家不能购买自己发布的闲置商品（防止刷单）
        if (goods.getSellerId().equals(userId)) return Result.error("不能购买自己的闲置商品");
        // 调用订单服务创建闲置商品购买订单（包含买家ID和商品信息）
        ordersService.addIdleOrder(userId, goods);
        // 调用闲置商品服务将商品状态标记为已售出
        idleGoodsService.markAsSold(id);
        // 返回购买成功的空结果
        return Result.success();
    }
}
