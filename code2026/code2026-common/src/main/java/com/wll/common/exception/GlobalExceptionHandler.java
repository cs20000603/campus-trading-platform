package com.wll.common.exception; // 声明包路径

import cn.hutool.log.Log; // Hutool的日志接口
import cn.hutool.log.LogFactory; // Hutool的日志工厂，用于创建Log实例
import com.wll.common.dto.Result; // 统一响应结果类
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象
import org.springframework.web.bind.annotation.ControllerAdvice; // 全局控制器增强
import org.springframework.web.bind.annotation.ExceptionHandler; // 异常处理器注解
import org.springframework.web.bind.annotation.ResponseBody; // 标识方法返回JSON
import org.springframework.web.servlet.resource.NoResourceFoundException; // 静态资源未找到异常


/**
 * 全局异常处理器
 * 使用@ControllerAdvice注解，拦截所有Controller抛出的异常，统一处理
 *
 * 为什么需要它？
 * 如果不在Controller中每个方法都写try-catch，异常会直接暴露给前端
 * 有了这个类，所有未捕获的异常都会在这里被拦截，转换为友好的Result.error()响应
 * Controller代码更简洁，不需要在每个方法中写异常处理逻辑
 */
@ControllerAdvice // 声明这是一个全局异常处理类，会拦截所有@Controller/@RestController的异常
public class GlobalExceptionHandler {

    /** 日志对象，用于记录异常信息到日志文件 */
    private static final Log log = LogFactory.get(); // 创建Hutool Log实例

    /**
     * 处理静态资源未找到异常
     * 浏览器会自动请求 /favicon.ico、/manifest.json 等静态资源
     * 这些资源不存在时不需要记录错误日志，直接返回"资源不存在"即可
     */
    @ExceptionHandler(NoResourceFoundException.class) // 指定只处理NoResourceFoundException异常
    @ResponseBody // 表示返回值将被自动转换为JSON格式
    public Result noResourceFound(HttpServletRequest request, NoResourceFoundException e) {
        return Result.error("资源不存在"); // 返回友好的错误提示，不记录日志
    }

    /**
     * 处理所有未被其他handler捕获的Exception异常（兜底处理）
     * 记录完整的异常堆栈到日志，返回通用错误响应
     */
    @ExceptionHandler(Exception.class) // 指定处理Exception及其所有子类异常
    @ResponseBody // 返回JSON格式
    public Result error(HttpServletRequest request, Exception e) {
        log.error("异常信息：", e); // 记录完整的异常堆栈信息到日志文件，方便排查问题
        return Result.error(); // 返回"500-请求失败"，不暴露具体的异常信息给前端（安全考虑）
    }

    /**
     * 处理自定义CustomException异常
     * 将CustomException中的msg字段作为错误消息返回给前端
     * 业务逻辑中主动抛出的错误信息（如"密码错误"、"用户不存在"）通过此方法返回
     */
    @ExceptionHandler(CustomException.class) // 指定只处理CustomException异常
    @ResponseBody // 返回JSON格式
    public Result customError(HttpServletRequest request, CustomException e) {
        return Result.error(e.getMsg()); // 从CustomException中取出msg，作为错误提示返回给前端
    }

}
