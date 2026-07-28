package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal，用于精确的金额计算（避免float/double浮点数精度问题）

/**
 * 商品实体类（核心业务实体）
 * 对应数据库中的goods表，存储平台上所有商品的信息
 * 包含价格、库存、分类、店铺归属、折扣等完整的商品属性
 * 是系统中最重要、字段最多的实体之一
 */
public class Goods {

    /** 商品主键ID，数据库自增，唯一标识每个商品 */
    private Integer id;
    /** 商品名称，如"美式咖啡"、"无线蓝牙耳机" */
    private String name;
    /** 商品主图片URL，指向MinIO中存储的商品图片 */
    private String img;
    /** 商品售价（BigDecimal精确金额），如12.00 */
    private BigDecimal price;
    /** 商品简介/简短描述，在列表页展示 */
    private String description;
    /** 商品详情（富文本HTML内容），在商品详情页展示，由wang-editor编辑器编辑 */
    private String content;
    /** 库存数量，下单时扣减，取消订单时恢复 */
    private Integer store;
    /** 所属分类ID，关联category表 */
    private Integer categoryId;
    /** 分类名称（冗余字段），避免每次查询都要JOIN category表 */
    private String categoryName;
    /** 上架状态："上架"表示前台可见可购买，"下架"表示不在前台展示 */
    private String status;
    /** 浏览量计数器，每次查看商品详情时+1 */
    private Integer views;
    /** 累计销量，每次成功下单时增加 */
    private Integer saleCount;
    /** 商品创建/上架时间 */
    private String time;
    /** 是否推荐标记，用于首页推荐位展示 */
    private String recommend;
    /** 所属店铺ID，关联shop表，标识这个商品是哪个店铺的 */
    private Integer shopId;
    /** 折扣价，如果设置了折扣价则在前台展示划线原价和折扣价 */
    private BigDecimal discountPrice;
    /** 折扣截止时间，超过此时间折扣自动失效 */
    private String discountEnd;

    // ===== 以下为 Getter/Setter 方法 =====
    public String getRecommend() { return recommend; }
    public void setRecommend(String recommend) { this.recommend = recommend; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getStore() { return store; }
    public void setStore(Integer store) { this.store = store; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getViews() { return views; }
    public void setViews(Integer views) { this.views = views; }

    public Integer getSaleCount() { return saleCount; }
    public void setSaleCount(Integer saleCount) { this.saleCount = saleCount; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }

    public BigDecimal getDiscountPrice() { return discountPrice; }
    public void setDiscountPrice(BigDecimal discountPrice) { this.discountPrice = discountPrice; }

    public String getDiscountEnd() { return discountEnd; }
    public void setDiscountEnd(String discountEnd) { this.discountEnd = discountEnd; }
}
