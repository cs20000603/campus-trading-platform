// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入验证码服务CaptchaService，负责生成图形验证码、存储验证码到Redis、验证码校验
import com.wll.common.service.CaptchaService;
// 导入统一响应结果类Result，用于封装API返回数据
import com.wll.common.dto.Result;
// Lombok的@Slf4j注解，编译时自动生成log静态字段（SLF4J日志门面），可直接使用log.info/log.error等
import lombok.extern.slf4j.Slf4j;
// Spring的@Autowired注解，按类型自动注入Bean（与@Resource功能类似，但默认按类型装配）
import org.springframework.beans.factory.annotation.Autowired;
// 导入@GetMapping注解，用于映射HTTP GET请求到指定方法
import org.springframework.web.bind.annotation.GetMapping;
// 导入@RequestMapping注解，用于定义控制器级别的请求路径前缀
import org.springframework.web.bind.annotation.RequestMapping;
// 导入@RequestParam注解，用于绑定URL查询参数到方法参数
import org.springframework.web.bind.annotation.RequestParam;
// 导入@RestController注解，标记该类为RESTful控制器
import org.springframework.web.bind.annotation.RestController;

/**
 * 验证码控制器
 * 负责生成管理员登录所需的图形验证码，验证码存储在Redis中并输出到后端控制台
 * 请求路径前缀：/captcha
 */
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将该控制器所有接口统一映射到/captcha前缀下
@RequestMapping("/captcha")
// @Slf4j注解（Lombok）：编译时自动生成private static final org.slf4j.Logger log字段，方便日志输出
@Slf4j
// 声明CaptchaController公共类
public class CaptchaController {

    // @Autowired注解：按类型自动注入CaptchaService Bean（与@Resource功能类似，Spring标准注解）
    @Autowired
    private CaptchaService captchaService;

    /**
     * 生成管理员登录验证码
     * 请求方式：GET /captcha/admin
     * 使用admin作为默认用户名生成验证码，验证码图片保存到本地文件系统，
     * 验证码文本存入Redis并输出到后端控制台供管理员查看
     * @return Result 提示验证码已生成的成功响应
     */
    // @GetMapping注解：将HTTP GET请求映射到该方法，请求路径为/captcha/admin
    @GetMapping("/admin")
    // getAdminCaptcha方法：生成管理员登录用的图形验证码，无需参数
    public Result getAdminCaptcha() {
        // 调用验证码服务，为admin用户生成验证码
        // generateAdminCaptcha内部逻辑：1) 生成随机4位验证码文本 2) 将验证码存入Redis（设置过期时间）
        // 3) 生成验证码图片保存到本地 4) 将验证码文本打印到后端控制台
        String captcha = captchaService.generateAdminCaptcha("admin");
        // 返回成功响应，提示管理员去后端控制台查看验证码
        return Result.success("验证码已生成，请查看后端控制台");
    }
}
