// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入收藏实体类，映射数据库中的收藏表
import com.wll.common.entity.Collect;
// 导入收藏服务接口，提供收藏的增删查等操作
import com.wll.common.service.CollectService;
// 导入PageHelper分页插件返回的分页信息类，包含分页数据和总条数等
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /collect
@RequestMapping("/collect")
// 收藏控制器，处理用户收藏商品/店铺的添加、删除、查询等请求
public class CollectController {

    // 通过@Resource注解注入收藏服务实例（按名称装配）
    @Resource
    // 收藏服务接口引用，用于调用收藏相关业务逻辑
    private CollectService collectService;

    // 映射POST请求到 /collect/add，用户添加收藏
    @PostMapping("/add")
    // @RequestBody将请求体JSON绑定到Collect实体，HttpServletRequest获取当前用户ID
    public Result add(@RequestBody Collect collect, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID，设置为收藏所属用户
        collect.setUserId((Integer) request.getAttribute("userId"));
        // 调用收藏服务执行添加操作
        collectService.add(collect);
        // 返回添加成功的空结果
        return Result.success();
    }

    // 映射DELETE请求到 /collect/delete/{id}，根据ID取消收藏
    @DeleteMapping("/delete/{id}")
    // @PathVariable从URL路径中提取收藏记录ID
    public Result deleteById(@PathVariable Integer id) {
        // 调用收藏服务根据ID删除指定收藏记录
        collectService.deleteById(id);
        // 返回删除成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /collect/selectAll，查询当前用户的所有收藏记录
    @GetMapping("/selectAll")
    // HttpServletRequest用于获取JWT拦截器设置的userId属性
    public Result selectAll(HttpServletRequest request) {
        // 创建收藏查询条件对象
        Collect collect = new Collect();
        // 设置查询条件：仅查询当前用户的收藏记录
        collect.setUserId((Integer) request.getAttribute("userId"));
        // 调用收藏服务按条件查询全部，并直接将结果包装为成功结果返回
        return Result.success(collectService.selectAll(collect));
    }

    // 映射GET请求到 /collect/selectPage，分页查询当前用户的收藏记录
    @GetMapping("/selectPage")
    // @RequestParam获取页码参数，defaultValue="1"指定默认值为第1页
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            // @RequestParam获取每页条数参数，defaultValue="10"指定默认每页10条
            @RequestParam(defaultValue = "10") Integer pageSize,
            // HttpServletRequest用于获取当前用户ID
            HttpServletRequest request) {
        // 创建收藏查询条件对象
        Collect collect = new Collect();
        // 设置查询条件：仅查询当前用户的收藏记录
        collect.setUserId((Integer) request.getAttribute("userId"));
        // 调用收藏服务进行分页查询，传入查询条件、页码和每页条数
        PageInfo<Collect> page = collectService.selectPage(collect, pageNum, pageSize);
        // 将分页结果（包含数据列表、总条数、总页数等）包装为成功结果返回
        return Result.success(page);
    }
}
