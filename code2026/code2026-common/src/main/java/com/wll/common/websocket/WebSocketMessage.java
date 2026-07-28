package com.wll.common.websocket;

import cn.hutool.core.date.DateUtil; // Hutool的日期工具，用于获取当前时间字符串

/**
 * WebSocket消息数据结构
 * 封装通过WebSocket推送给客户端的消息，包含事件类型、目标用户、内容、时间
 * 最终被JSONUtil.toJsonStr()序列化为JSON字符串发送
 */
public class WebSocketMessage {
    /** 事件类型（枚举），前端据此区分通知类别 */
    private WebSocketEventType eventType;
    /** 目标用户ID，标识消息发给谁 */
    private Integer targetUserId;
    /** 消息文本内容，展示给用户看 */
    private String message;
    /** 消息时间戳，格式如"2026-07-01 14:30:25"（使用Hutool的DateUtil.now()） */
    private String timestamp;

    /** 无参构造函数（JSON反序列化需要） */
    public WebSocketMessage() {}

    /**
     * 全参构造函数，自动填充当前时间
     * @param eventType 事件类型
     * @param targetUserId 目标用户ID
     * @param message 消息内容
     */
    public WebSocketMessage(WebSocketEventType eventType, Integer targetUserId, String message) {
        this.eventType = eventType;       // 设置事件类型
        this.targetUserId = targetUserId; // 设置目标用户
        this.message = message;           // 设置消息内容
        this.timestamp = DateUtil.now();  // 自动取当前时间，格式"yyyy-MM-dd HH:mm:ss"
    }

    // ===== Getter/Setter =====
    public WebSocketEventType getEventType() { return eventType; }
    public void setEventType(WebSocketEventType eventType) { this.eventType = eventType; }
    public Integer getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Integer targetUserId) { this.targetUserId = targetUserId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
