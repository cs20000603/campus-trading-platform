package com.wll.common.entity; // 声明包路径

/**
 * 商品分类实体类
 * 对应数据库中的category表，用于管理商品的分类体系
 * 不同店铺类型（如饮品、烘焙）可以使用不同的分类
 */
public class Category {

    /** 分类主键ID，数据库自增 */
    private Integer id;
    /** 分类名称，如"咖啡系列"、"奶茶系列"、"蛋糕系列"等 */
    private String name;
    /** 所属店铺类型，如"饮品"、"烘焙"、"服饰"等
     *  用于让不同类型的店铺展示各自专属的商品分类 */
    private String shopType;

    /** 获取分类ID */
    public Integer getId() {
        return id;
    }

    /** 设置分类ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取分类名称 */
    public String getName() {
        return name;
    }

    /** 设置分类名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取店铺类型 */
    public String getShopType() {
        return shopType;
    }

    /** 设置店铺类型 */
    public void setShopType(String shopType) {
        this.shopType = shopType;
    }
}
