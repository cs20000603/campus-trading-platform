package com.wll.common.entity; // 声明包路径

/**
 * 店铺实体类
 * 对应数据库中的shop表，存储商家店铺信息
 * 店铺是平台商家入驻的核心数据，包含审核流程相关状态
 * 店铺审核通过后商家才能上架商品进行销售
 */
public class Shop {

    /** 店铺主键ID，数据库自增 */
    private Integer id;
    /** 店铺名称，如"校园咖啡屋"、"甜蜜烘焙坊" */
    private String name;
    /** 店铺简介/描述 */
    private String description;
    /** 店铺Logo图片URL，存储在MinIO中 */
    private String logo;
    /** 店主用户ID，关联user表 */
    private Integer ownerId;
    /** 店铺状态："线上审核中"（等待管理员审核）、"营业中"（审核通过正常经营）、"审核拒绝"（被驳回） */
    private String status;
    /** 店铺联系电话 */
    private String phone;
    /** 店铺地址，如"一食堂旁"、"学生活动中心一楼" */
    private String address;
    /** 店铺创建/申请时间 */
    private String createTime;
    /** 店铺类型，如"饮品"、"烘焙"、"服饰"、"数码"等 */
    private String type;
    /** 经营许可证图片URL，商家申请开店时上传 */
    private String license;
    /** 审核拒绝原因，管理员驳回申请时填写，供店主修改参考 */
    private String rejectReason;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public Integer getOwnerId() { return ownerId; }
    public void setOwnerId(Integer ownerId) { this.ownerId = ownerId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLicense() { return license; }
    public void setLicense(String license) { this.license = license; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
}
