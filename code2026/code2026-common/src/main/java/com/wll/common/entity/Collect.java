package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal

/**
 * 用户收藏实体类
 * 对应数据库中的collect表，记录用户收藏的商品
 * 用户可以在商品详情页点击收藏，在个人中心查看收藏列表
 * 冗余了商品名称、图片、价格，方便收藏列表直接展示
 */
public class Collect {

    /** 收藏记录主键ID，数据库自增 */
    private Integer id;
    /** 被收藏的商品ID，关联goods表 */
    private Integer goodsId;
    /** 商品名称，冗余字段 */
    private String goodsName;
    /** 商品图片URL，冗余字段 */
    private String goodsImg;
    /** 商品价格（收藏时的价格，BigDecimal精确金额） */
    private BigDecimal goodsPrice;
    /** 收藏者用户ID */
    private Integer userId;
    /** 收藏者用户名，冗余字段 */
    private String userName;
    /** 收藏时间，格式如"2026-07-01 14:30:25" */
    private String time;

    /** 获取商品图片 */
    public String getGoodsImg() {
        return goodsImg;
    }

    /** 设置商品图片 */
    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    /** 获取商品价格 */
    public BigDecimal getGoodsPrice() {
        return goodsPrice;
    }

    /** 设置商品价格 */
    public void setGoodsPrice(BigDecimal goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    /** 获取商品名称 */
    public String getGoodsName() {
        return goodsName;
    }

    /** 设置商品名称 */
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    /** 获取收藏者用户名 */
    public String getUserName() {
        return userName;
    }

    /** 设置收藏者用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取收藏记录ID */
    public Integer getId() {
        return id;
    }

    /** 设置收藏记录ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取被收藏商品ID */
    public Integer getGoodsId() {
        return goodsId;
    }

    /** 设置被收藏商品ID */
    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    /** 获取收藏者用户ID */
    public Integer getUserId() {
        return userId;
    }

    /** 设置收藏者用户ID */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /** 获取收藏时间 */
    public String getTime() {
        return time;
    }

    /** 设置收藏时间 */
    public void setTime(String time) {
        this.time = time;
    }
}
