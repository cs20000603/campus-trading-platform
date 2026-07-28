// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.utils;

// 导入JJWT库的Claims类，表示JWT令牌解析后的载荷数据
import io.jsonwebtoken.Claims;
// 导入HttpServletRequest，用于获取HTTP请求中的Header信息（如Authorization头）
import jakarta.servlet.http.HttpServletRequest;
// 导入HttpServletResponse，用于设置HTTP响应状态码（如401未授权）
import jakarta.servlet.http.HttpServletResponse;
// 导入Spring的Component注解，标记该类为Spring容器管理的组件
import org.springframework.stereotype.Component;
// 导入Spring MVC的HandlerInterceptor接口，用于实现请求拦截器
import org.springframework.web.servlet.HandlerInterceptor;

// 标记该类为Spring组件，被Spring容器扫描和管理
@Component
// JWT拦截器类，实现HandlerInterceptor接口，在请求到达Controller前进行JWT身份验证
public class JwtInterceptor implements HandlerInterceptor {

    // JWT工具类实例，用于验证和解析JWT令牌（通过构造器注入）
    private final JwtUtils jwtUtils;

    // 构造器注入JWT工具类依赖
    public JwtInterceptor(JwtUtils jwtUtils) {
        // 保存注入的JWT工具类实例
        this.jwtUtils = jwtUtils;
    }

    // 重写HandlerInterceptor接口的preHandle方法，在Controller方法执行前调用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 处理CORS预检请求（OPTIONS方法），直接放行不需要JWT验证
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            // 返回true表示放行该请求
            return true;
        }

        // 从HTTP请求头中获取Authorization字段的值
        String token = request.getHeader("Authorization");
        // 校验token是否为空，以及是否以"Bearer "开头（标准JWT格式：Bearer <token>）
        if (token == null || !token.startsWith("Bearer ")) {
            // token无效或格式不对，设置HTTP响应状态码为401（未授权）
            response.setStatus(401);
            // 返回false表示拦截该请求，不再继续处理
            return false;
        }

        // 使用try-catch捕获JWT验证过程中可能发生的异常（如令牌过期、签名错误等）
        try {
            // 去掉前缀"Bearer "（7个字符），提取纯token字符串
            token = token.substring(7);
            // 调用JWT工具类验证并解析令牌，获取载荷数据
            Claims claims = jwtUtils.validateToken(token);
            // 从JWT载荷中提取userId，并设置为请求属性，供后续Controller和Service使用
            request.setAttribute("userId", claims.get("userId", Integer.class));
            // 返回true表示验证通过，放行请求
            return true;
        } catch (Exception e) {
            // JWT验证失败（过期、签名错误等），设置HTTP响应状态码为401
            response.setStatus(401);
            // 返回false表示拦截该请求
            return false;
        }
    }
}
