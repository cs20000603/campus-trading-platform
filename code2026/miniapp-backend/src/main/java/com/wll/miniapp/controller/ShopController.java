// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入Hutool日期工具类，用于获取当前日期时间字符串
import cn.hutool.core.date.DateUtil;
// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入店铺实体类，映射数据库中的店铺表
import com.wll.common.entity.Shop;
// 导入用户实体类，映射数据库中的用户表
import com.wll.common.entity.User;
// 导入店铺数据访问接口，用于店铺的增删改查操作
import com.wll.common.mapper.ShopMapper;
// 导入用户数据访问接口，用于用户信息的查询操作
import com.wll.common.mapper.UserMapper;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入List集合类，用于存储查询到的数据列表
import java.util.List;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /shop
@RequestMapping("/shop")
// 店铺控制器，处理店铺的注册申请、列表查询、详情查看、我的店铺、更新、类型查询等请求
public class ShopController {

    // 通过@Resource注解注入店铺数据访问实例（按名称装配）
    @Resource
    // 店铺数据访问接口引用，用于直接操作店铺表
    private ShopMapper shopMapper;
    // 通过@Resource注解注入用户数据访问实例（按名称装配）
    @Resource
    // 用户数据访问接口引用，用于查询用户信息
    private UserMapper userMapper;

    // 映射POST请求到 /shop/register，用户申请注册店铺（提交开店申请）
    @PostMapping("/register")
    // @RequestBody将请求体JSON绑定到Shop实体，HttpServletRequest获取当前用户ID
    public Result register(@RequestBody Shop shop, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 查询当前用户是否已经申请过店铺（一个用户只能开一个店铺）
        // Check if user already has a shop
        Shop existing = shopMapper.selectByOwnerId(userId);
        // 判断是否已存在店铺申请记录
        if (existing != null) {
            // 如果已有申请记录，返回错误提示
            return Result.error("您已经申请过店铺");
        }
        // 设置店铺的所有者ID为当前登录用户
        shop.setOwnerId(userId);
        // 设置店铺初始状态为"线上审核中"，需要管理员审核通过后才能营业
        shop.setStatus("线上审核中");
        // 使用Hutool工具获取当前日期时间字符串，设置为店铺创建时间
        shop.setCreateTime(DateUtil.now());
        // 将店铺申请信息插入数据库
        shopMapper.insert(shop);
        // 返回申请成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /shop/list，查询所有营业中的店铺（用于首页展示）
    @GetMapping("/list")
    // 查询营业中店铺列表的方法
    public Result list() {
        // 创建店铺查询条件对象
        Shop query = new Shop();
        // 设置查询条件：仅查询状态为"营业中"的店铺
        query.setStatus("营业中");
        // 按条件查询所有匹配的店铺列表
        List<Shop> list = shopMapper.selectAll(query);
        // 将店铺列表包装为成功结果返回
        return Result.success(list);
    }

    // 映射GET请求到 /shop/detail/{id}，根据ID查看店铺详情
    @GetMapping("/detail/{id}")
    // @PathVariable从URL路径中提取店铺ID
    public Result detail(@PathVariable Integer id) {
        // 根据店铺ID查询店铺的详细信息
        Shop shop = shopMapper.selectById(id);
        // 将店铺详情包装为成功结果返回
        return Result.success(shop);
    }

    // 映射GET请求到 /shop/my，查询当前用户拥有的店铺信息
    @GetMapping("/my")
    // HttpServletRequest获取当前用户ID
    public Result myShop(HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据所有者ID（即当前用户ID）查询该用户拥有的店铺
        Shop shop = shopMapper.selectByOwnerId(userId);
        // 将店铺信息包装为成功结果返回（可能为null，表示用户还没有店铺）
        return Result.success(shop);
    }

    // 映射PUT请求到 /shop/update，更新店铺信息（如被拒后重新提交）
    @PutMapping("/update")
    // @RequestBody将请求体JSON绑定到Shop实体，HttpServletRequest获取当前用户ID用于权限校验
    public Result update(@RequestBody Shop shop, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据实体中的ID查询数据库中已存在的店铺记录
        Shop dbShop = shopMapper.selectById(shop.getId());
        // 校验店铺是否存在，以及当前用户是否为该店铺的所有者
        if (dbShop == null || !dbShop.getOwnerId().equals(userId)) {
            // 校验不通过，返回无权限错误
            return Result.error("无权操作");
        }
        // 如果原店铺状态为"审核拒绝"，说明是重新提交申请
        // 如果拒绝后重新提交，重置状态并清除驳回理由
        if ("审核拒绝".equals(dbShop.getStatus())) {
            // 重新提交后状态恢复为"线上审核中"
            shop.setStatus("线上审核中");
            // 清除上次的驳回理由
            shop.setRejectReason(null);
        }
        // 执行更新操作，将修改后的店铺信息写入数据库
        shopMapper.updateById(shop);
        // 返回更新成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /shop/types，查询所有不重复的店铺类型（用于筛选）
    @GetMapping("/types")
    // 查询所有店铺类型的方法
    public Result types() {
        // 调用店铺数据访问接口查询所有不重复的店铺类型列表
        List<String> types = shopMapper.selectDistinctTypes();
        // 将类型列表包装为成功结果返回
        return Result.success(types);
    }
}
