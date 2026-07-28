// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入商品分类实体类，映射数据库中的分类表
import com.wll.common.entity.Category;
// 导入商品分类服务接口，提供分类查询等业务操作
import com.wll.common.service.CategoryService;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入List集合类，用于存储查询到的分类数据列表
import java.util.List;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /category
@RequestMapping("/category")
// 商品分类控制器，处理商品分类数据的查询请求
public class CategoryController {

    // 通过@Resource注解注入分类服务实例（按名称装配）
    @Resource
    // 分类服务接口引用，用于调用分类相关业务逻辑
    private CategoryService categoryService;

    // 映射GET请求到 /category/selectAll，查询所有商品分类
    @GetMapping("/selectAll")
    // 查询全部商品分类列表的方法
    public Result selectAll() {
        // 调用分类服务查询所有分类，传入null表示无过滤条件
        List<Category> list = categoryService.selectAll(null);
        // 将查询到的分类列表包装为成功结果返回
        return Result.success(list);
    }

    // 映射GET请求到 /category/selectByShopType，根据店铺类型筛选分类
    @GetMapping("/selectByShopType")
    // @RequestParam获取URL查询参数shopType，required=false表示该参数可选
    public Result selectByShopType(@RequestParam(required = false) String shopType) {
        // 调用分类服务按店铺类型查询分类列表
        List<Category> list = categoryService.selectByShopType(shopType);
        // 将查询到的分类列表包装为成功结果返回
        return Result.success(list);
    }
}
