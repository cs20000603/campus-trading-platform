// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.config;

// 导入JWT握手拦截器，用于在WebSocket握手阶段进行JWT认证
import com.wll.miniapp.websocket.JwtHandshakeInterceptor;
// 导入小程序WebSocket消息处理器，处理WebSocket连接和消息
import com.wll.miniapp.websocket.MiniAppWsHandler;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入Spring的Configuration注解，标记该类为配置类
import org.springframework.context.annotation.Configuration;
// 导入Spring WebSocket的EnableWebSocket注解，启用WebSocket支持
import org.springframework.web.socket.config.annotation.EnableWebSocket;
// 导入WebSocketConfigurer接口，用于注册WebSocket处理器和拦截器
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
// 导入WebSocketHandlerRegistry类，用于注册WebSocket处理器到URL路径
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

// 标记该类为Spring配置类，在应用启动时被加载处理
@Configuration
// 启用Spring WebSocket支持（基于JSR-356标准）
@EnableWebSocket
// 小程序WebSocket配置类，实现WebSocketConfigurer接口来注册WebSocket处理器
public class MiniAppWebSocketConfig implements WebSocketConfigurer {

    // 通过@Resource注解注入小程序WebSocket消息处理器实例
    @Resource
    // WebSocket处理器引用，处理客户端连接、消息、断开等事件
    private MiniAppWsHandler miniAppWsHandler;

    // 通过@Resource注解注入JWT握手拦截器实例
    @Resource
    // JWT握手拦截器引用，在WebSocket连接建立前验证JWT令牌
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    // 重写WebSocketConfigurer接口的方法，用于注册WebSocket处理器和拦截器
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 向注册中心添加WebSocket处理器，映射到路径 /ws/miniapp
        registry.addHandler(miniAppWsHandler, "/ws/miniapp")
                // 为这个WebSocket端点添加JWT握手拦截器（在握手阶段验证身份）
                .addInterceptors(jwtHandshakeInterceptor)
                // 设置允许跨域访问的来源为 "*"（允许所有域名连接WebSocket）
                .setAllowedOrigins("*");
    }
}
