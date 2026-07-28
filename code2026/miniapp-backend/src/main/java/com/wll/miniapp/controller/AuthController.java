// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入用户实体类，映射数据库中的用户表
import com.wll.common.entity.User;
// 导入用户数据访问接口，提供用户表的CRUD操作
import com.wll.common.mapper.UserMapper;
// 导入认证服务类，处理微信登录、手机登录、注册等业务逻辑
import com.wll.miniapp.service.AuthService;
// 导入HttpServletRequest，用于获取HTTP请求信息（如JWT拦截器设置的userId属性）
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入Map集合类，用于接收前端传递的键值对请求参数
import java.util.Map;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /auth
@RequestMapping("/auth")
// 认证控制器，处理微信登录、账号密码登录、手机登录、注册、绑定手机、重置密码、获取用户信息等请求
public class AuthController {

    // 认证服务实例，处理登录注册等核心认证业务逻辑（通过构造器注入）
    private final AuthService authService;
    // 用户数据访问接口实例，直接操作用户数据库表（通过构造器注入）
    private final UserMapper userMapper;

    // 构造器注入依赖，Spring会自动注入AuthService和UserMapper的实现
    public AuthController(AuthService authService, UserMapper userMapper) {
        // 保存注入的认证服务实例
        this.authService = authService;
        // 保存注入的用户数据访问实例
        this.userMapper = userMapper;
    }

    // 映射POST请求到 /auth/login，处理微信小程序一键登录
    @PostMapping("/login")
    // @RequestBody将请求体中的JSON数据绑定到Map参数
    public Result wxLogin(@RequestBody Map<String, String> params) {
        // 使用try-catch捕获登录过程中可能发生的异常
        try {
            // 从前端请求参数中获取微信登录授权码code
            String code = params.get("code");
            // 校验授权码是否为空
            if (code == null || code.isEmpty()) {
                // 如果授权码为空，返回错误提示
                return Result.error("缺少code参数");
            }
            // 调用认证服务的微信登录方法，传入授权码进行登录
            Map<String, Object> data = authService.wxLogin(code);
            // 将登录成功返回的用户数据和token包装为成功结果
            return Result.success(data);
        } catch (Exception e) {
            // 打印异常堆栈信息到控制台，方便排查问题
            e.printStackTrace();
            // 返回登录失败的通用错误信息，包含具体异常消息
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    // 映射POST请求到 /auth/accountLogin，处理账号（用户名/手机号）+密码登录
    @PostMapping("/accountLogin")
    // @RequestBody将请求体中的JSON数据绑定到Map参数
    public Result accountLogin(@RequestBody Map<String, String> params) {
        // 使用try-catch捕获登录过程中可能发生的异常
        try {
            // 从请求参数中获取账号（支持用户名或手机号）
            String account = params.get("account");
            // 从请求参数中获取密码
            String password = params.get("password");
            // 校验账号是否为空或仅包含空白字符
            if (account == null || account.trim().isEmpty()) {
                // 如果账号为空，返回错误提示
                return Result.error("请输入手机号或用户名");
            }
            // 校验密码是否为空
            if (password == null || password.isEmpty()) {
                // 如果密码为空，返回错误提示
                return Result.error("请输入密码");
            }
            // 调用认证服务的账号登录方法，传入处理后的账号和密码
            Map<String, Object> data = authService.accountLogin(account.trim(), password);
            // 将登录成功返回的用户数据和token包装为成功结果
            return Result.success(data);
        } catch (Exception e) {
            // 打印异常堆栈信息到控制台
            e.printStackTrace();
            // 返回登录失败的错误信息
            return Result.error(e.getMessage());
        }
    }

    // 映射POST请求到 /auth/phoneLogin，处理手机号+密码登录
    @PostMapping("/phoneLogin")
    // @RequestBody将请求体中的JSON数据绑定到Map参数
    public Result phoneLogin(@RequestBody Map<String, String> params) {
        // 使用try-catch捕获登录过程中可能发生的异常
        try {
            // 从请求参数中获取手机号
            String phone = params.get("phone");
            // 从请求参数中获取密码
            String password = params.get("password");
            // 校验手机号是否为空
            if (phone == null || phone.isEmpty()) {
                // 如果手机号为空，返回错误提示
                return Result.error("请输入手机号");
            }
            // 校验密码是否为空
            if (password == null || password.isEmpty()) {
                // 如果密码为空，返回错误提示
                return Result.error("请输入密码");
            }
            // 调用认证服务的手机登录方法，传入手机号和密码
            Map<String, Object> data = authService.phoneLogin(phone, password);
            // 将登录成功返回的用户数据和token包装为成功结果
            return Result.success(data);
        } catch (Exception e) {
            // 打印异常堆栈信息到控制台
            e.printStackTrace();
            // 返回登录失败的错误信息
            return Result.error(e.getMessage());
        }
    }

    // 映射POST请求到 /auth/register，处理手机号注册
    @PostMapping("/register")
    // @RequestBody将请求体中的JSON数据绑定到Map参数
    public Result register(@RequestBody Map<String, String> params) {
        // 使用try-catch捕获注册过程中可能发生的异常
        try {
            // 从请求参数中获取手机号
            String phone = params.get("phone");
            // 从请求参数中获取密码
            String password = params.get("password");
            // 校验手机号长度是否为11位（中国大陆手机号标准）
            if (phone == null || phone.length() != 11) {
                // 如果手机号格式不正确，返回错误提示
                return Result.error("请输入正确的手机号");
            }
            // 校验密码长度是否至少6位
            if (password == null || password.length() < 6) {
                // 如果密码长度不足，返回错误提示
                return Result.error("密码至少6位");
            }
            // 调用认证服务的注册方法，传入手机号和密码
            Map<String, Object> data = authService.register(phone, password);
            // 将注册成功返回的用户数据和token包装为成功结果
            return Result.success(data);
        } catch (Exception e) {
            // 打印异常堆栈信息到控制台
            e.printStackTrace();
            // 返回注册失败的错误信息
            return Result.error(e.getMessage());
        }
    }

    // 映射POST请求到 /auth/bindPhone，为微信登录用户绑定手机号和密码
    @PostMapping("/bindPhone")
    // @RequestBody绑定请求体JSON，HttpServletRequest获取JWT拦截器设置的userId属性
    public Result bindPhone(@RequestBody Map<String, String> params, HttpServletRequest request) {
        // 使用try-catch捕获绑定过程中可能发生的异常
        try {
            // 从请求属性中获取JWT拦截器解析出的当前用户ID
            Integer userId = (Integer) request.getAttribute("userId");
            // 从请求参数中获取要绑定的手机号
            String phone = params.get("phone");
            // 从请求参数中获取要设置的密码
            String password = params.get("password");
            // 校验手机号是否为空
            if (phone == null || phone.isEmpty()) {
                // 如果手机号为空，返回错误提示
                return Result.error("请输入手机号");
            }
            // 校验密码是否为空
            if (password == null || password.isEmpty()) {
                // 如果密码为空，返回错误提示
                return Result.error("请设置密码");
            }
            // 调用认证服务绑定手机号，传入用户ID、手机号和密码
            authService.bindPhone(userId, phone, password);
            // 绑定成功后返回成功结果（无数据负载）
            return Result.success();
        } catch (Exception e) {
            // 打印异常堆栈信息到控制台
            e.printStackTrace();
            // 返回绑定失败的错误信息
            return Result.error(e.getMessage());
        }
    }

    // 映射POST请求到 /auth/resetPassword，通过手机号重置密码（无需登录状态）
    @PostMapping("/resetPassword")
    // @RequestBody将请求体中的JSON数据绑定到Map参数
    public Result resetPassword(@RequestBody Map<String, String> params) {
        // 使用try-catch捕获重置密码过程中可能发生的异常
        try {
            // 从请求参数中获取手机号
            String phone = params.get("phone");
            // 从请求参数中获取新密码
            String newPassword = params.get("newPassword");
            // 校验手机号是否为空
            if (phone == null || phone.isEmpty()) {
                // 如果手机号为空，返回错误提示
                return Result.error("请输入手机号");
            }
            // 校验新密码长度是否至少6位
            if (newPassword == null || newPassword.length() < 6) {
                // 如果新密码长度不足，返回错误提示
                return Result.error("新密码至少6位");
            }
            // 通过手机号查询用户是否存在
            User user = userMapper.selectByPhone(phone);
            // 判断用户是否为空（即该手机号是否已注册）
            if (user == null) {
                // 如果用户不存在，返回错误提示
                return Result.error("该手机号未注册");
            }
            // 将新密码设置到用户实体中
            user.setPassword(newPassword);
            // 更新用户信息到数据库，完成密码重置
            userMapper.updateById(user);
            // 返回成功结果
            return Result.success();
        } catch (Exception e) {
            // 打印异常堆栈信息到控制台
            e.printStackTrace();
            // 返回重置密码失败的错误信息
            return Result.error(e.getMessage());
        }
    }

    // 映射GET请求到 /auth/userInfo，获取当前登录用户的详细信息
    @GetMapping("/userInfo")
    // HttpServletRequest用于获取JWT拦截器设置的userId属性
    public Result getUserInfo(HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 通过用户ID查询完整的用户信息
        User user = userMapper.selectById(userId);
        // 判断用户是否存在
        if (user != null) {
            // 出于安全考虑，将密码字段置空，不返回给前端
            user.setPassword(null);
        }
        // 将用户信息包装为成功结果返回
        return Result.success(user);
    }
}
