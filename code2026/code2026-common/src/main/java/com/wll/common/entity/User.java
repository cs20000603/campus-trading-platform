package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal，用于精确的金额计算（避免浮点数精度问题）

/**
 * 普通用户实体类
 * 继承Account父类，额外增加了账户余额、微信openid、JWT令牌三个字段
 * 对应数据库中的user表，存储所有普通用户（买家、商家）的信息
 */
public class User extends Account { // 继承Account父类
    /** 用户主键ID */
    private Integer id;
    /** 登录用户名 */
    private String username;
    /** 登录密码 */
    private String password;
    /** 用户昵称/真实姓名 */
    private String name;
    /** 头像URL */
    private String avatar;
    /** 角色：普通用户/商家 */
    private String role;
    /** 账户余额，BigDecimal保证金额计算精度（不用float/double） */
    private BigDecimal account;
    /** 微信小程序openid，微信用户在小程序中的唯一标识 */
    private String openid;
    /** JWT认证令牌，登录成功后生成并存储，后续请求携带此令牌验证身份 */
    private String token;

    /** 获取头像URL */
    public String getAvatar() {
        return avatar;
    }

    /** 设置头像URL */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /** 获取用户ID */
    public Integer getId() {
        return id;
    }

    /** 设置用户ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取用户名 */
    public String getUsername() {
        return username;
    }

    /** 设置用户名 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 获取密码 */
    public String getPassword() {
        return password;
    }

    /** 设置密码 */
    public void setPassword(String password) {
        this.password = password;
    }

    /** 获取昵称 */
    public String getName() {
        return name;
    }

    /** 设置昵称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取角色 */
    public String getRole() {
        return role;
    }

    /** 设置角色 */
    public void setRole(String role) {
        this.role = role;
    }

    /** 获取账户余额（BigDecimal类型，精确金额） */
    public BigDecimal getAccount() {
        return account;
    }

    /** 设置账户余额 */
    public void setAccount(BigDecimal account) {
        this.account = account;
    }

    /** 获取微信openid */
    public String getOpenid() {
        return openid;
    }

    /** 设置微信openid */
    public void setOpenid(String openid) {
        this.openid = openid;
    }

    /** 获取JWT令牌 */
    public String getToken() {
        return token;
    }

    /** 设置JWT令牌 */
    public void setToken(String token) {
        this.token = token;
    }
}
