package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal精确金额

/**
 * 闲置商品实体类（校园二手交易）
 * 对应数据库中的idle_goods表，用户发布的二手/闲置物品出售信息
 * 包含成色、配送方式、校区等校园二手交易特有的字段
 * 与IdleWanted（求购）共同构成校园闲置交易模块
 */
public class IdleGoods {

    /** 闲置商品主键ID，数据库自增 */
    private Integer id;
    /** 闲置商品标题，如"九成新iPhone 16 Pro" */
    private String title;
    /** 商品详细描述 */
    private String description;
    /** 多张图片URL，用逗号分隔，如"url1,url2,url3" */
    private String images;
    /** 售价（BigDecimal精确金额） */
    private BigDecimal price;
    /** 原价，用于展示折扣力度 */
    private BigDecimal originalPrice;
    /** 商品成色，如"全新"、"九成新"、"七成新"等 */
    private String condition;
    /** 配送方式，如"自取"、"快递到付"、"校内面交"等 */
    private String deliveryType;
    /** 所在校区/宿舍区域，如"东校区"、"北苑宿舍" */
    private String campusArea;
    /** 闲置商品分类，如"电子产品"、"书籍"、"生活用品"等 */
    private String category;
    /** 商品状态："在售"表示可购买、"已售出"表示已卖出、"已下架"表示卖家主动下架 */
    private String status;
    /** 卖家用户ID */
    private Integer sellerId;
    /** 卖家用户名，冗余字段 */
    private String sellerName;
    /** 卖家头像URL，冗余字段 */
    private String sellerAvatar;
    /** 关联店铺ID（可为空，普通用户直接发布不关联店铺） */
    private Integer shopId;
    /** 店铺名称，冗余字段 */
    private String shopName;
    /** 浏览量（每次查看详情+1） */
    private Integer views;
    /** 发布时间 */
    private String createTime;
    /** 售出时间（商品被购买时记录） */
    private String soldTime;

    // ===== Getter/Setter 方法 =====

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public String getCampusArea() { return campusArea; }
    public void setCampusArea(String campusArea) { this.campusArea = campusArea; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getSellerId() { return sellerId; }
    public void setSellerId(Integer sellerId) { this.sellerId = sellerId; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerAvatar() { return sellerAvatar; }
    public void setSellerAvatar(String sellerAvatar) { this.sellerAvatar = sellerAvatar; }

    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getSoldTime() { return soldTime; }
    public void setSoldTime(String soldTime) { this.soldTime = soldTime; }
}
