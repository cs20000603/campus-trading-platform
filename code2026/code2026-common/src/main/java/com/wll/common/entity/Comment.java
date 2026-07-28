package com.wll.common.entity; // 声明包路径

/**
 * 商品评论实体类
 * 对应数据库中的comment表，记录用户对已购买商品的评价
 * 用户下单并确认收货后可以发表评论，包含评分和文字内容
 */
public class Comment {

    /** 评论主键ID，数据库自增 */
    private Integer id;
    /** 评分（1~5分，Double类型支持小数如4.5分） */
    private Double score;
    /** 评论文字内容 */
    private String content;
    /** 评论者用户ID */
    private Integer userId;
    /** 评论者用户名，冗余字段 */
    private String userName;
    /** 评论者头像URL，冗余字段 */
    private String userAvatar;
    /** 评论时间 */
    private String time;
    /** 关联的订单ID，用于追溯该评论来自哪个订单 */
    private Integer orderId;
    /** 关联的订单编号，冗余字段方便展示 */
    private String orderNo;
    /** 被评价的商品ID */
    private Integer goodsId;
    /** 商品名称，冗余字段 */
    private String goodsName;
    /** 商品图片，冗余字段 */
    private String goodsImg;

    /** 获取评论者头像 */
    public String getUserAvatar() {
        return userAvatar;
    }

    /** 设置评论者头像 */
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    /** 获取评论ID */
    public Integer getId() {
        return id;
    }

    /** 设置评论ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取评分 */
    public Double getScore() {
        return score;
    }

    /** 设置评分 */
    public void setScore(Double score) {
        this.score = score;
    }

    /** 获取评论内容 */
    public String getContent() {
        return content;
    }

    /** 设置评论内容 */
    public void setContent(String content) {
        this.content = content;
    }

    /** 获取评论者ID */
    public Integer getUserId() {
        return userId;
    }

    /** 设置评论者ID */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /** 获取评论者用户名 */
    public String getUserName() {
        return userName;
    }

    /** 设置评论者用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取评论时间 */
    public String getTime() {
        return time;
    }

    /** 设置评论时间 */
    public void setTime(String time) {
        this.time = time;
    }

    /** 获取关联订单ID */
    public Integer getOrderId() {
        return orderId;
    }

    /** 设置关联订单ID */
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    /** 获取订单编号 */
    public String getOrderNo() {
        return orderNo;
    }

    /** 设置订单编号 */
    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    /** 获取被评价商品ID */
    public Integer getGoodsId() {
        return goodsId;
    }

    /** 设置被评价商品ID */
    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    /** 获取商品名称 */
    public String getGoodsName() {
        return goodsName;
    }

    /** 设置商品名称 */
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    /** 获取商品图片 */
    public String getGoodsImg() {
        return goodsImg;
    }

    /** 设置商品图片 */
    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }
}
