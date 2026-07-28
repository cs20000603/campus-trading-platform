// 声明该类所属的包路径，com.wll.config包存放Spring配置类
package com.wll.config;

// 导入Spring的@Configuration注解，标记该类为Spring配置类（相当于XML中的<beans>），Spring容器会在启动时加载该类
import org.springframework.context.annotation.Configuration;
// 导入Spring消息模块的MessageBrokerRegistry类，用于配置STOMP消息代理（消息中转站）的参数
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
// 导入@EnableWebSocketMessageBroker注解，启用基于STOMP协议的WebSocket消息代理功能
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
// 导入StompEndpointRegistry类，用于注册STOMP协议端点（WebSocket连接入口），配置跨域和SockJS支持
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
// 导入WebSocketMessageBrokerConfigurer接口，实现该接口来自定义WebSocket消息代理的配置
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// @Configuration注解：标记该类为Spring配置类，Spring容器启动时会自动加载并执行其中的@Bean和配置方法
@Configuration
// @EnableWebSocketMessageBroker注解：启用Spring的基于STOMP协议的WebSocket消息代理支持
// 该注解使Spring能处理@MessageMapping注解的方法（类似@Controller中的@MessageMapping），并配置消息代理路由
@EnableWebSocketMessageBroker
// 声明StompWebSocketConfig公共类
// 实现WebSocketMessageBrokerConfigurer接口：允许重写configureMessageBroker和registerStompEndpoints方法来定制WebSocket消息代理行为
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理（Message Broker）
     * 重写父接口的configureMessageBroker方法，设置消息代理的前缀和目的地规则
     * @param config MessageBrokerRegistry对象，用于注册消息代理的配置（启用简单代理、设置目的地前缀等）
     */
    // @Override注解：标记该方法重写了父接口WebSocketMessageBrokerConfigurer中的方法
    @Override
    // configureMessageBroker方法：配置STOMP消息代理的内部路由规则
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // enableSimpleBroker：启用基于内存的简单消息代理
        // 参数"/topic"：以/topic为前缀的目的地会广播给所有订阅者（一对多，用于系统公告、管理员通知等）
        // 参数"/queue"：以/queue为前缀的目的地用于点对点消息（一对一，用于向特定用户推送通知）
        config.enableSimpleBroker("/topic", "/queue");
        // setApplicationDestinationPrefixes：设置应用程序目的地前缀为/app
        // 客户端发送消息到/app/xxx，消息会被路由到@MessageMapping("/xxx")注解的处理器方法
        config.setApplicationDestinationPrefixes("/app");
        // setUserDestinationPrefix：设置用户目的地前缀为/user
        // 搭配@SendToUser注解或SimpMessagingTemplate.convertAndSendToUser方法使用
        // 当向/user/{username}/queue/xxx发送消息时，Spring会自动解析为对应用户的私有队列
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点（WebSocket连接入口）
     * 重写父接口的registerStompEndpoints方法，配置客户端建立WebSocket连接的URL端点
     * @param registry StompEndpointRegistry对象，用于注册STOMP端点，设置连接URL、跨域规则和SockJS回退支持
     */
    // @Override注解：标记重写方法
    @Override
    // registerStompEndpoints方法：注册WebSocket握手端点（客户端连接此URL建立WebSocket连接）
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // addEndpoint("/ws")：注册一个STOMP端点，客户端通过 /ws 路径建立WebSocket连接
        registry.addEndpoint("/ws")
                // setAllowedOriginPatterns("*")：设置允许的跨域来源模式，*表示允许任意来源的跨域请求
                // 使用Patterns方式（而非setAllowedOrigins），支持通配符，避免浏览器CORS拦截
                .setAllowedOriginPatterns("*")
                // withSockJS()：启用SockJS回退支持
                // 当浏览器不支持原生WebSocket时，SockJS会自动降级使用HTTP长轮询等替代方案
                .withSockJS();
    }
}
