package com.wll.common.entity; // 声明包路径

/**
 * 闲置商品私信消息实体类
 * 对应数据库中的idle_message表，存储闲置商品买卖双方的聊天消息
 * 买家看到感兴趣的闲置商品后可通过私信与卖家沟通
 */
public class IdleMessage {

    /** 消息主键ID，数据库自增 */
    private Integer id;
    /** 关联的闲置商品ID，标识这条消息是关于哪个商品的 */
    private Integer idleId;
    /** 发送者用户ID */
    private Integer senderId;
    /** 发送者用户名，冗余字段 */
    private String senderName;
    /** 发送者头像URL，冗余字段 */
    private String senderAvatar;
    /** 接收者用户ID */
    private Integer receiverId;
    /** 消息文本内容 */
    private String content;
    /** 是否已读：0表示未读，1表示已读 */
    private Integer isRead;
    /** 消息发送时间 */
    private String createTime;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdleId() { return idleId; }
    public void setIdleId(Integer idleId) { this.idleId = idleId; }

    public Integer getSenderId() { return senderId; }
    public void setSenderId(Integer senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public Integer getReceiverId() { return receiverId; }
    public void setReceiverId(Integer receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getIsRead() { return isRead; }
    public void setIsRead(Integer isRead) { this.isRead = isRead; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}
