package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal精确金额

/**
 * 订单详情实体类
 * 对应数据库中的order_detail表，存储订单中每个商品的具体购买信息
 * 一个订单（Orders）可以包含多个订单详情（OrderDetail），一个订单详情对应一个商品
 * 例如：一个订单买了"美式咖啡x2 + 提拉米苏x1"，则会有两条OrderDetail记录
 */
public class OrderDetail {

    /** 订单详情主键ID，数据库自增 */
    private Integer id;
    /** 商品ID，关联goods表查询商品信息 */
    private Integer goodsId;
    /** 该商品的购买数量 */
    private Integer num;
    /** 所属订单ID，关联orders表 */
    private Integer orderId;
    /** 商品图片URL，冗余字段方便展示 */
    private String goodsImg;
    /** 商品名称，冗余字段方便展示 */
    private String goodsName;
    /** 商品单价，冗余字段记录下单时的价格 */
    private BigDecimal goodsPrice;

    /** 获取商品单价 */
    public BigDecimal getGoodsPrice() {
        return goodsPrice;
    }

    /** 设置商品单价 */
    public void setGoodsPrice(BigDecimal goodsPrice) {
        this.goodsPrice = goodsPrice;
    }

    /** 获取订单详情ID */
    public Integer getId() {
        return id;
    }

    /** 设置订单详情ID */
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

    /** 获取所属订单ID */
    public Integer getOrderId() {
        return orderId;
    }

    /** 设置所属订单ID */
    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    /** 获取商品图片 */
    public String getGoodsImg() {
        return goodsImg;
    }

    /** 设置商品图片 */
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
}
