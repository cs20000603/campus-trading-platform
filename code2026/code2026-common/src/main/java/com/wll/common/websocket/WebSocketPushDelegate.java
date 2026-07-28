package com.wll.common.websocket;

/**
 * WebSocket推送代理接口
 * 定义向指定用户推送JSON消息的方法规范
 * miniapp-backend模块的MiniAppWsDelegate实现此接口，
 * 将自己注册到WebSocketPushService中，实现跨模块的消息推送协作
 */
public interface WebSocketPushDelegate {
    /**
     * 向指定用户推送消息
     * @param userId 接收消息的用户ID
     * @param jsonMessage 已序列化为JSON字符串的消息内容
     */
    void pushToUser(Integer userId, String jsonMessage);
}
