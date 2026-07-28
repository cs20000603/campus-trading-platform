// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入商品实体类，映射数据库中的商品表
import com.wll.common.entity.Goods;
// 导入商品服务接口，提供商品的查询等操作
import com.wll.common.service.GoodsService;
// 导入PageHelper分页插件返回的分页信息类
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入List集合类，用于存储查询到的商品数据列表
import java.util.List;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /goods
@RequestMapping("/goods")
// 商品控制器，处理商品的查询（全部、分页、按ID、搜索建议）等请求（增删改在admin模块）
public class GoodsController {

    // 通过@Resource注解注入商品服务实例（按名称装配）
    @Resource
    // 商品服务接口引用，用于调用商品相关业务逻辑
    private GoodsService goodsService;

    // 映射GET请求到 /goods/selectAll，根据条件查询全部商品
    @GetMapping("/selectAll")
    // Goods实体作为查询条件接收参数（如shopId按店铺筛选、name模糊查询等）
    public Result selectAll(Goods goods) {
        // 调用商品服务按条件查询全部商品列表
        List<Goods> list = goodsService.selectAll(goods);
        // 将查询到的商品列表包装为成功结果返回
        return Result.success(list);
    }

    // 映射GET请求到 /goods/selectPage，分页查询商品
    @GetMapping("/selectPage")
    // Goods实体作为查询条件接收参数（如categoryId按分类筛选）
    public Result selectPage(Goods goods,
                             // @RequestParam获取页码参数，默认第1页
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam获取每页条数参数，默认每页10条
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用商品服务进行分页查询，传入查询条件、页码和每页条数
        PageInfo<Goods> page = goodsService.selectPage(goods, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }

    // 映射GET请求到 /goods/selectById/{id}，根据商品ID查询商品详情
    @GetMapping("/selectById/{id}")
    // @PathVariable从URL路径中提取商品ID
    public Result selectById(@PathVariable Integer id) {
        // 调用商品服务根据ID查询单个商品
        Goods goods = goodsService.selectById(id);
        // 将查询到的商品详情包装为成功结果返回
        return Result.success(goods);
    }

    // 映射GET请求到 /goods/suggest，根据输入的关键词提供搜索建议/自动补全
    @GetMapping("/suggest")
    // @RequestParam从URL查询参数中获取关键词
    public Result suggest(@RequestParam String keyword) {
        // 调用商品服务的搜索建议方法，返回匹配的商品名称列表
        List<String> list = goodsService.suggest(keyword);
        // 将搜索建议列表包装为成功结果返回
        return Result.success(list);
    }
}
