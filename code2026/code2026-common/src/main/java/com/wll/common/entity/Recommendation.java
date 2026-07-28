package com.wll.common.entity; // 声明包路径

/**
 * AI商品推荐实体类
 * 对应数据库中的recommendation表，存储AI根据用户偏好生成的个性化商品推荐
 * 包含推荐理由和置信度评分，供前端展示"猜你喜欢"等推荐模块
 */
public class Recommendation {

    /** 推荐记录主键ID，数据库自增 */
    private Integer id;
    /** 被推荐的用户ID */
    private Integer userId;
    /** 用户名，冗余字段 */
    private String userName;
    /** 推荐商品的ID，关联goods表 */
    private Integer goodsId;
    /** 推荐商品名称，冗余字段 */
    private String goodsName;
    /** 推荐商品图片，冗余字段 */
    private String goodsImg;
    /** 推荐理由，如"根据您的浏览记录推荐"、"与您购买过的商品相似"等 */
    private String reason;
    /** 推荐置信度评分（0~1之间的Double值，越大越推荐） */
    private Double score;
    /** 推荐生成时间 */
    private String time;

    /** 获取推荐记录ID */
    public Integer getId() {
        return id;
    }

    /** 设置推荐记录ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取被推荐用户ID */
    public Integer getUserId() {
        return userId;
    }

    /** 设置被推荐用户ID */
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

    /** 获取推荐商品ID */
    public Integer getGoodsId() {
        return goodsId;
    }

    /** 设置推荐商品ID */
    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    /** 获取推荐商品名称 */
    public String getGoodsName() {
        return goodsName;
    }

    /** 设置推荐商品名称 */
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    /** 获取推荐商品图片 */
    public String getGoodsImg() {
        return goodsImg;
    }

    /** 设置推荐商品图片 */
    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    /** 获取推荐理由 */
    public String getReason() {
        return reason;
    }

    /** 设置推荐理由 */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /** 获取推荐置信度 */
    public Double getScore() {
        return score;
    }

    /** 设置推荐置信度 */
    public void setScore(Double score) {
        this.score = score;
    }

    /** 获取推荐时间 */
    public String getTime() {
        return time;
    }

    /** 设置推荐时间 */
    public void setTime(String time) {
        this.time = time;
    }
}
