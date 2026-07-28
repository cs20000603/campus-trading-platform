// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.websocket;

// 导入JWT工具类，用于在WebSocket握手阶段验证和解析JWT令牌
import com.wll.miniapp.utils.JwtUtils;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入Spring的ServerHttpRequest接口，表示WebSocket握手阶段的HTTP请求
import org.springframework.http.server.ServerHttpRequest;
// 导入Spring的ServerHttpResponse接口，表示WebSocket握手阶段的HTTP响应
import org.springframework.http.server.ServerHttpResponse;
// 导入Spring的ServletServerHttpRequest类，用于从WebSocket HTTP请求中获取原始Servlet请求
import org.springframework.http.server.ServletServerHttpRequest;
// 导入Spring的Component注解，标记该类为Spring容器管理的组件
import org.springframework.stereotype.Component;
// 导入Spring的WebSocketHandler接口，表示WebSocket消息处理器
import org.springframework.web.socket.WebSocketHandler;
// 导入Spring的HandshakeInterceptor接口，用于在WebSocket握手前后执行自定义逻辑
import org.springframework.web.socket.server.HandshakeInterceptor;

// 导入Map接口，用于向WebSocket会话属性中传递数据（如userId）
import java.util.Map;

// 标记该类为Spring组件，被Spring容器扫描和管理
@Component
// JWT WebSocket握手拦截器，在WebSocket连接建立前的HTTP握手阶段进行JWT身份验证
// 实现HandshakeInterceptor接口，可以在握手前后插入自定义认证逻辑
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    // 通过@Resource注解注入JWT工具类实例（按名称装配）
    @Resource
    // JWT工具类引用，用于解析和验证JWT令牌中的用户ID
    private JwtUtils jwtUtils;

    // 重写HandshakeInterceptor接口的beforeHandshake方法，在WebSocket握手之前执行
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   // WebSocket处理器对象
                                   WebSocketHandler wsHandler,
                                   // WebSocket会话的属性Map，可以在此向会话传递数据（如userId）
                                   Map<String, Object> attributes) {
        // 判断HTTP请求是否为ServletServerHttpRequest类型（即来自Servlet容器的WebSocket升级请求）
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 从原始Servlet请求的Header中获取Authorization字段的值
            String authHeader = servletRequest.getServletRequest().getHeader("Authorization");
            // 校验Authorization头是否为空以及是否以"Bearer "开头（标准JWT格式）
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // 去掉前缀"Bearer "（7个字符），提取纯JWT token字符串
                String token = authHeader.substring(7);
                // 使用try-catch捕获JWT解析和验证过程中可能发生的异常
                try {
                    // 调用JWT工具类从token中提取用户ID
                    Integer userId = jwtUtils.getUserId(token);
                    // 将用户ID放入WebSocket会话属性中，供后续WebSocket处理器使用
                    attributes.put("userId", userId);
                    // 返回true表示握手认证通过，允许建立WebSocket连接
                    return true;
                } catch (Exception e) {
                    // JWT验证失败（令牌过期、签名错误等），返回false拒绝WebSocket握手
                    return false;
                }
            }
        }
        // 如果没有Authorization头或不是Servlet请求，返回false拒绝握手
        return false;
    }

    // 重写HandshakeInterceptor接口的afterHandshake方法，在WebSocket握手完成后执行（无论成功或失败）
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               // WebSocket处理器对象
                               WebSocketHandler wsHandler,
                               // 握手过程中可能发生的异常，成功时为null
                               Exception exception) {
        // 此方法为空实现，握手完成后不需要执行额外的逻辑
    }
}
