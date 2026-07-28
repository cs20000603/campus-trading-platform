package com.wll.common.entity; // 声明包路径

/**
 * 首页轮播图实体类
 * 对应数据库中的carousel表，管理首页展示的轮播图片
 * 轮播图可以关联到具体商品，用户点击轮播图可跳转到商品详情页
 */
public class Carousel {

    /** 轮播图主键ID，数据库自增 */
    private Integer id;
    /** 关联的商品ID，点击轮播图后跳转到该商品的详情页 */
    private Integer goodsId;
    /** 关联的商品名称，冗余字段，避免每次查询都要关联goods表 */
    private String goodsName;
    /** 轮播图图片URL，指向MinIO中存储的图片文件 */
    private String img;

    /** 获取轮播图ID */
    public Integer getId() {
        return id;
    }

    /** 设置轮播图ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取关联商品ID */
    public Integer getGoodsId() {
        return goodsId;
    }

    /** 设置关联商品ID */
    public void setGoodsId(Integer goodsId) {
        this.goodsId = goodsId;
    }

    /** 获取关联商品名称 */
    public String getGoodsName() {
        return goodsName;
    }

    /** 设置关联商品名称 */
    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    /** 获取轮播图片URL */
    public String getImg() {
        return img;
    }

    /** 设置轮播图片URL */
    public void setImg(String img) {
        this.img = img;
    }
}
