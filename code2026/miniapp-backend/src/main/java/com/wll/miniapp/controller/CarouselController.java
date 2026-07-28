// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入轮播图实体类，映射数据库中的轮播图表
import com.wll.common.entity.Carousel;
// 导入轮播图服务接口，提供轮播图查询等业务操作
import com.wll.common.service.CarouselService;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入List集合类，用于存储查询到的轮播图列表
import java.util.List;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /carousel
@RequestMapping("/carousel")
// 轮播图控制器，处理首页轮播图数据的查询请求
public class CarouselController {

    // 通过@Resource注解注入轮播图服务实例（按名称装配）
    @Resource
    // 轮播图服务接口引用，用于调用轮播图相关业务逻辑
    private CarouselService carouselService;

    // 映射GET请求到 /carousel/selectAll，查询所有轮播图数据（包含禁用状态的由service层过滤）
    @GetMapping("/selectAll")
    // 查询全部轮播图列表的方法
    public Result selectAll() {
        // 调用轮播图服务查询所有记录，传入null表示查询全部
        List<Carousel> list = carouselService.selectAll(null);
        // 将查询到的轮播图列表包装为成功结果返回
        return Result.success(list);
    }
}
