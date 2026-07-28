// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入账户实体类（用于修改密码时接收旧密码和新密码参数）
import com.wll.common.entity.Account;
// 导入用户实体类，映射数据库中的用户表
import com.wll.common.entity.User;
// 导入用户服务接口，提供用户信息查询、更新、修改密码等操作
import com.wll.common.service.UserService;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 注意：此控制器未使用@RequestMapping类级别注解，各方法分别定义完整路径
// 用户控制器，处理用户信息查询、更新、修改密码等请求
public class UserController {

    // 通过@Resource注解注入用户服务实例（按名称装配）
    @Resource
    // 用户服务接口引用，用于调用用户相关业务逻辑
    private UserService userService;

    // 映射GET请求到 /user/selectById/{id}，根据用户ID查看用户信息（公开访问）
    @GetMapping("/user/selectById/{id}")
    // @PathVariable从URL路径中提取用户ID
    public Result selectById(@PathVariable Integer id) {
        // 调用用户服务根据ID查询用户信息
        User user = userService.selectById(id);
        // 判断用户是否存在
        if (user != null) {
            // 出于安全考虑，将密码字段置空，不返回给前端
            user.setPassword(null);
        }
        // 将用户信息包装为成功结果返回
        return Result.success(user);
    }

    // 映射PUT请求到 /user/update，当前登录用户修改自己的个人信息
    @PutMapping("/user/update")
    // @RequestBody将请求体JSON绑定到User实体，HttpServletRequest获取当前用户ID
    public Result update(@RequestBody User user, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID，强制设置为要更新的用户ID
        // 防止用户通过伪造ID修改他人信息
        user.setId((Integer) request.getAttribute("userId"));
        // 调用用户服务执行更新操作
        userService.update(user);
        // 返回更新成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /user/info，获取当前登录用户的详细信息（需登录）
    @GetMapping("/user/info")
    // HttpServletRequest用于获取JWT拦截器设置的userId属性
    public Result getUserInfo(HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 通过用户ID查询完整的用户信息
        User user = userService.selectById(userId);
        // 判断用户是否存在
        if (user != null) {
            // 出于安全考虑，将密码字段置空，不返回给前端
            user.setPassword(null);
        }
        // 将用户信息包装为成功结果返回
        return Result.success(user);
    }

    // 映射PUT请求到 /updatePassword，当前登录用户修改自己的密码
    @PutMapping("/updatePassword")
    // @RequestBody将请求体JSON绑定到Account实体（包含旧密码和新密码），HttpServletRequest获取当前用户ID
    public Result updatePassword(@RequestBody Account account, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 校验用户是否已登录（userId为空说明JWT未通过验证或未传入token）
        if (userId == null) {
            // 如果未登录，返回错误提示
            return Result.error("未登录");
        }
        // 设置账户实体的ID为当前用户ID
        account.setId(userId);
        // 设置账户实体的角色为"普通用户"（区分管理员和普通用户的密码修改逻辑）
        account.setRole("普通用户");
        // 调用用户服务执行密码修改操作（服务层会校验旧密码是否正确）
        userService.updatePassword(account);
        // 返回修改成功的空结果
        return Result.success();
    }
}
