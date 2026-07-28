// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入闲置求购实体类，映射数据库中的闲置求购信息表
import com.wll.common.entity.IdleWanted;
// 导入闲置求购服务接口，提供求购信息的增删改查等操作
import com.wll.common.service.IdleWantedService;
// 导入PageHelper分页插件返回的分页信息类
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入List集合类，用于存储查询到的求购数据列表
import java.util.List;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /idleWanted
@RequestMapping("/idleWanted")
// 闲置求购控制器，处理校园闲置交易中求购信息的发布、删除、修改、查询等请求
public class IdleWantedController {

    // 通过@Resource注解注入闲置求购服务实例（按名称装配）
    @Resource
    // 闲置求购服务接口引用，用于调用求购相关业务逻辑
    private IdleWantedService idleWantedService;

    // 映射POST请求到 /idleWanted/add，发布一条求购信息（我想买什么）
    @PostMapping("/add")
    // @RequestBody将请求体JSON绑定到IdleWanted实体，HttpServletRequest获取当前用户ID
    public Result add(@RequestBody IdleWanted idleWanted, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 设置求购信息的发布者ID为当前登录用户
        idleWanted.setUserId(userId);
        // 调用求购服务执行发布操作
        idleWantedService.add(idleWanted);
        // 返回发布成功的空结果
        return Result.success();
    }

    // 映射DELETE请求到 /idleWanted/delete/{id}，删除自己发布的求购信息
    @DeleteMapping("/delete/{id}")
    // @PathVariable从URL路径中提取求购信息ID，HttpServletRequest获取当前用户ID用于权限校验
    public Result deleteById(@PathVariable Integer id, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据ID查询要删除的求购信息
        IdleWanted wanted = idleWantedService.selectById(id);
        // 校验求购信息是否存在，不存在则返回错误
        if (wanted == null) return Result.error("信息不存在");
        // 校验当前用户是否为该求购信息的发布者（仅发布者有权删除），非法操作则返回错误
        if (!wanted.getUserId().equals(userId)) return Result.error("无权操作");
        // 权限校验通过，执行删除操作
        idleWantedService.deleteById(id);
        // 返回删除成功的空结果
        return Result.success();
    }

    // 映射PUT请求到 /idleWanted/update，修改自己发布的求购信息
    @PutMapping("/update")
    // @RequestBody将请求体JSON绑定到IdleWanted实体（含要修改的ID和新数据），HttpServletRequest获取当前用户ID
    public Result updateById(@RequestBody IdleWanted idleWanted, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 根据实体中的ID查询数据库中已存在的求购记录
        IdleWanted exist = idleWantedService.selectById(idleWanted.getId());
        // 校验求购信息是否存在
        if (exist == null) return Result.error("信息不存在");
        // 校验当前用户是否为该求购信息的发布者（仅发布者有权修改）
        if (!exist.getUserId().equals(userId)) return Result.error("无权操作");
        // 权限校验通过，执行更新操作
        idleWantedService.updateById(idleWanted);
        // 返回更新成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /idleWanted/selectAll，根据条件查询全部求购信息
    @GetMapping("/selectAll")
    // IdleWanted实体作为查询条件接收参数（如keyword模糊搜索、category分类筛选等）
    public Result selectAll(IdleWanted idleWanted) {
        // 调用求购服务按条件查询全部匹配的求购信息列表
        List<IdleWanted> list = idleWantedService.selectAll(idleWanted);
        // 将查询到的求购列表包装为成功结果返回
        return Result.success(list);
    }

    // 映射GET请求到 /idleWanted/selectPage，分页查询求购信息
    @GetMapping("/selectPage")
    // IdleWanted实体作为查询条件接收参数
    public Result selectPage(IdleWanted idleWanted,
                             // @RequestParam获取页码参数，默认第1页
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam获取每页条数参数，默认每页10条
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用求购服务进行分页查询
        PageInfo<IdleWanted> page = idleWantedService.selectPage(idleWanted, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }

    // 映射GET请求到 /idleWanted/myWanted，分页查询当前用户发布的求购信息
    @GetMapping("/myWanted")
    // @RequestParam获取页码参数，默认第1页
    public Result myWanted(@RequestParam(defaultValue = "1") Integer pageNum,
                           // @RequestParam获取每页条数参数，默认每页10条
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           // HttpServletRequest获取当前用户ID
                           HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 创建求购查询条件对象
        IdleWanted query = new IdleWanted();
        // 设置查询条件：仅查询当前用户发布的求购信息
        query.setUserId(userId);
        // 调用求购服务进行分页查询，返回当前用户的发布列表
        PageInfo<IdleWanted> page = idleWantedService.selectPage(query, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }
}
