package com.wll.common.entity; // 声明包路径

/**
 * AI聊天消息实体类
 * 对应数据库中的chat_message表，存储用户与AI智能助手的对话历史
 * role字段区分消息是用户发的("user")还是AI回复的("assistant")
 * 可用于前端展示对话记录和上下文记忆
 */
public class ChatMessage {

    /** 聊天消息主键ID，数据库自增 */
    private Integer id;
    /** 用户ID，标识这条消息属于哪个用户 */
    private Integer userId;
    /** 用户名，冗余字段 */
    private String userName;
    /** 用户头像URL，冗余字段 */
    private String userAvatar;
    /** 角色标识："user"表示用户发的消息，"assistant"表示AI助手的回复 */
    private String role;
    /** 消息文本内容 */
    private String content;
    /** 消息发送/接收时间 */
    private String time;

    /** 获取消息ID */
    public Integer getId() {
        return id;
    }

    /** 设置消息ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取用户ID */
    public Integer getUserId() {
        return userId;
    }

    /** 设置用户ID */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /** 获取用户名 */
    public String getUserName() {
        return userName;
    }

    /** 设置用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取用户头像 */
    public String getUserAvatar() {
        return userAvatar;
    }

    /** 设置用户头像 */
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    /** 获取消息角色（user/assistant） */
    public String getRole() {
        return role;
    }

    /** 设置消息角色 */
    public void setRole(String role) {
        this.role = role;
    }

    /** 获取消息内容 */
    public String getContent() {
        return content;
    }

    /** 设置消息内容 */
    public void setContent(String content) {
        this.content = content;
    }

    /** 获取消息时间 */
    public String getTime() {
        return time;
    }

    /** 设置消息时间 */
    public void setTime(String time) {
        this.time = time;
    }
}
