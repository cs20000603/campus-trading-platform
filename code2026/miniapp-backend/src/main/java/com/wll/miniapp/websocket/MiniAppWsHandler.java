// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.websocket;

// 导入Spring的Component注解，标记该类为Spring容器管理的组件
import org.springframework.stereotype.Component;
// 导入Spring WebSocket的CloseStatus类，表示WebSocket连接的关闭状态（状态码和原因）
import org.springframework.web.socket.CloseStatus;
// 导入Spring WebSocket的TextMessage类，表示文本格式的WebSocket消息
import org.springframework.web.socket.TextMessage;
// 导入Spring WebSocket的WebSocketSession接口，表示一个已建立的WebSocket会话
import org.springframework.web.socket.WebSocketSession;
// 导入Spring WebSocket的TextWebSocketHandler类，专门处理文本消息的WebSocket处理器基类
import org.springframework.web.socket.handler.TextWebSocketHandler;

// 导入IOException类，用于处理发送消息时可能发生的IO异常
import java.io.IOException;
// 导入ConcurrentHashMap类，线程安全的HashMap，用于存储在线用户的WebSocket会话
import java.util.concurrent.ConcurrentHashMap;

// 标记该类为Spring组件，被Spring容器扫描和管理
@Component
// 小程序WebSocket消息处理器，继承TextWebSocketHandler处理文本类型的WebSocket消息
// 管理所有在线用户的WebSocket会话，提供连接建立、关闭、消息处理和主动推送功能
public class MiniAppWsHandler extends TextWebSocketHandler {

    // 使用ConcurrentHashMap存储所有在线用户的WebSocket会话映射
    // Key为Integer类型的用户ID，Value为对应的WebSocketSession会话对象
    // static表示所有MiniAppWsHandler实例共享；final表示引用不可变
    // ConcurrentHashMap保证多线程环境下的线程安全
    private static final ConcurrentHashMap<Integer, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // 重写TextWebSocketHandler的方法，WebSocket连接成功建立后调用
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从WebSocket会话属性中获取握手拦截器存入的用户ID
        Integer userId = (Integer) session.getAttributes().get("userId");
        // 判断用户ID是否有效（不为null）
        if (userId != null) {
            // 将用户ID和WebSocket会话的映射关系存入sessions中，表示该用户已上线
            sessions.put(userId, session);
        }
    }

    // 重写TextWebSocketHandler的方法，WebSocket连接关闭后调用
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 从WebSocket会话属性中获取用户ID
        Integer userId = (Integer) session.getAttributes().get("userId");
        // 判断用户ID是否有效
        if (userId != null) {
            // 从sessions中移除该用户的会话记录，表示该用户已下线
            sessions.remove(userId);
        }
    }

    // 重写TextWebSocketHandler的方法，收到客户端发来的文本消息时调用
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // 此处可处理客户端发来的ping/pong心跳消息，当前为空实现
        // ping/pong can be handled here if needed
    }

    // 静态方法，向指定用户ID推送JSON格式的文本消息（由MiniAppWsDelegate和其他服务调用）
    public static void sendToUser(Integer userId, String jsonMessage) {
        // 从sessions中获取指定用户ID对应的WebSocket会话
        WebSocketSession session = sessions.get(userId);
        // 判断会话是否存在且处于打开状态
        if (session != null && session.isOpen()) {
            // 使用try-catch捕获发送消息时可能发生的IO异常
            try {
                // 通过WebSocket会话向客户端发送TextMessage类型的消息
                session.sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                // 发送失败（如客户端网络断开），从sessions中移除该用户的会话记录
                sessions.remove(userId);
            }
        }
    }
}
