package com.wll.common.websocket;

import cn.hutool.json.JSONUtil; // Hutool的JSON工具
import org.springframework.beans.factory.annotation.Autowired; // @Autowired自动注入
import org.springframework.messaging.simp.SimpMessagingTemplate; // Spring的STOMP消息发送模板
import org.springframework.stereotype.Service; // @Service注解

/**
 * WebSocket消息推送服务（门面模式，统一推送入口）
 * 同时支持两种推送通道：
 * 1. STOMP通道 → 浏览器端（Web管理后台），通过SimpMessagingTemplate发送
 * 2. 代理通道 → 小程序端，通过注册的WebSocketPushDelegate发送
 *
 * 业务代码只需注入此Service，调用pushToUser或pushToTopic即可
 */
@Service // 注册为Spring Bean
public class WebSocketPushService {

    /** STOMP消息模板，required=false表示如果没有SimpMessagingTemplate也不报错 */
    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    /** 小程序端推送代理（由MiniAppWsDelegate在启动时注册） */
    private WebSocketPushDelegate delegate;

    /**
     * 注册小程序端的推送代理
     * @param delegate 实现了WebSocketPushDelegate接口的代理对象
     */
    public void registerDelegate(WebSocketPushDelegate delegate) {
        this.delegate = delegate; // 保存代理引用
    }

    /**
     * 向指定用户推送消息（点对点）
     * 将WebSocketMessage序列化为JSON后，同时通过STOMP和代理两条通道发送
     * @param userId 接收消息的用户ID
     * @param msg WebSocketMessage消息对象
     */
    public void pushToUser(Integer userId, WebSocketMessage msg) {
        if (msg.getTargetUserId() == null) { // 如果消息中没有设置目标用户
            msg.setTargetUserId(userId);     // 自动用参数userId填充
        }
        String json = JSONUtil.toJsonStr(msg); // 将消息对象序列化为JSON字符串
        if (messagingTemplate != null) { // 如果STOMP通道可用（springboot-backend模块）
            // 向 /user/{userId}/queue/notifications 发送消息
            messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),   // 用户ID转字符串
                "/queue/notifications",   // 目标地址
                json                      // JSON消息体
            );
        }
        if (delegate != null) { // 如果小程序代理已注册（miniapp-backend模块）
            delegate.pushToUser(userId, json); // 通过代理发送到小程序端
        }
    }

    /**
     * 向指定主题（Topic）广播消息
     * 所有订阅了该主题的在线用户都能收到
     * @param topic 主题路径，如"/topic/admin"、"/topic/idleSquare"
     * @param msg WebSocketMessage消息对象
     */
    public void pushToTopic(String topic, WebSocketMessage msg) {
        if (messagingTemplate != null) { // STOMP通道可用
            messagingTemplate.convertAndSend(topic, msg); // 广播到指定Topic
        }
    }
}
