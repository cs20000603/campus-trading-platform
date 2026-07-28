package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal

/**
 * 求购信息实体类（校园闲置物品的"我想要"功能）
 * 对应数据库中的idle_wanted表，用户发布自己想要购买的闲置物品需求
 * 与IdleGoods（出售闲置）互补，形成完整的校园二手交易供需闭环
 */
public class IdleWanted {

    /** 求购信息主键ID，数据库自增 */
    private Integer id;
    /** 求购标题，简要描述想要什么，如"求购二手自行车" */
    private String title;
    /** 详细描述，说明具体要求、期望成色等 */
    private String description;
    /** 预算金额（BigDecimal精确金额），用户愿意出的最高价格 */
    private BigDecimal budget;
    /** 求购分类，如"电子产品"、"书籍教材"、"生活用品"等 */
    private String category;
    /** 所在校区/宿舍区域，方便同校交易 */
    private String campusArea;
    /** 发布者用户ID */
    private Integer userId;
    /** 发布者用户名，冗余字段 */
    private String userName;
    /** 发布者头像URL，冗余字段 */
    private String userAvatar;
    /** 求购状态，如"求购中"，表示是否还在寻找 */
    private String status;
    /** 发布时间 */
    private String createTime;

    /** 获取求购ID */
    public Integer getId() {
        return id;
    }

    /** 设置求购ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取求购标题 */
    public String getTitle() {
        return title;
    }

    /** 设置求购标题 */
    public void setTitle(String title) {
        this.title = title;
    }

    /** 获取详细描述 */
    public String getDescription() {
        return description;
    }

    /** 设置详细描述 */
    public void setDescription(String description) {
        this.description = description;
    }

    /** 获取预算金额 */
    public BigDecimal getBudget() {
        return budget;
    }

    /** 设置预算金额 */
    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    /** 获取求购分类 */
    public String getCategory() {
        return category;
    }

    /** 设置求购分类 */
    public void setCategory(String category) {
        this.category = category;
    }

    /** 获取所在校区 */
    public String getCampusArea() {
        return campusArea;
    }

    /** 设置所在校区 */
    public void setCampusArea(String campusArea) {
        this.campusArea = campusArea;
    }

    /** 获取发布者ID */
    public Integer getUserId() {
        return userId;
    }

    /** 设置发布者ID */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /** 获取发布者用户名 */
    public String getUserName() {
        return userName;
    }

    /** 设置发布者用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取发布者头像 */
    public String getUserAvatar() {
        return userAvatar;
    }

    /** 设置发布者头像 */
    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    /** 获取求购状态 */
    public String getStatus() {
        return status;
    }

    /** 设置求购状态 */
    public void setStatus(String status) {
        this.status = status;
    }

    /** 获取发布时间 */
    public String getCreateTime() {
        return createTime;
    }

    /** 设置发布时间 */
    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
