package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal，精确金额计算

/**
 * 购物车实体类
 * 对应数据库中的cart表，存储用户加入购物车的商品信息
 * 用户可以将商品加入购物车暂存，然后统一结算下单
 * 冗余了商品名称、图片、价格字段，避免每次展示购物车都要查goods表
 */
public class Cart {

    /** 购物车记录主键ID，数据库自增 */
    private Integer id;
    /** 商品ID，关联goods表 */
    private Integer goodsId;
    /** 商品名称，冗余字段，方便购物车列表直接展示 */
    private String goodsName;
    /** 商品图片URL，冗余字段，方便购物车列表展示商品缩略图 */
    private String goodsImg;
    /** 商品单价（加入购物车时的价格，BigDecimal保证金额精度） */
    private BigDecimal goodsPrice;
    /** 购买数量，用户选择购买几件该商品 */
    private Integer num;
    /** 用户ID，标识这个购物车记录属于哪个用户 */
    private Integer userId;

    /** 获取商品单价 */
    public BigDecimal getGoodsPrice() {
        return goodsPrice;
    }

    /** 设置商品单价 */
    public void setGoodsPrice(BigDecimal goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    /** 获取商品图片URL */
    public String getGoodsImg() {
        return goodsImg;
    }

    /** 设置商品图片URL */
    public void setGoodsImg(String goodsImg) {
        this.goodsImg = goodsImg;
    }

    /** 获取商品名称 */
    public String getGoodsName() {
        return goodsName;
    }

    /** 设置商品名称 */
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    /** 获取购物车记录ID */
    public Integer getId() {
        return id;
    }

    /** 设置购物车记录ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取商品ID */
    public Integer getGoodsId() {
        return goodsId;
    }

    /** 设置商品ID */
    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    /** 获取购买数量 */
    public Integer getNum() {
        return num;
    }

    /** 设置购买数量 */
    public void setNum(Integer num) {
        this.num = num;
    }

    /** 获取用户ID（购物车所有者） */
    public Integer getUserId() {
        return userId;
    }

    /** 设置用户ID（购物车所有者） */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
