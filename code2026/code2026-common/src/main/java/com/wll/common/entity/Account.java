package com.wll.common.entity; // 声明包路径，属于公共模块的实体层

/**
 * 账号父类（抽象基类）
 * Admin（管理员）和 User（普通用户）都继承此类，共享通用字段
 * 作用：统一管理所有用户角色共有的属性，避免代码重复
 */
public class Account {
    /** 主键ID，数据库自增，唯一标识每条记录 */
    private Integer id;
    /** 用户名，用于登录的唯一凭证 */
    private String username;
    /** 昵称/真实姓名，用于页面展示 */
    private String name;
    /** 登录密码，明文存储 */
    private String password;
    /** 角色标识，如"管理员"、"普通用户"、"商家"等 */
    private String role;
    /** 新密码，用于修改密码时暂存新密码的值（不持久化到数据库） */
    private String newPassword;
    /** 头像URL，指向MinIO中存储的头像图片地址 */
    private String avatar;
    /** 手机号，可用于登录和接收通知 */
    private String phone;
    /** 验证码，用于登录时的验证码校验（不持久化到数据库） */
    private String captcha;

    // ===== 以下为手动编写的 Getter/Setter 方法 =====
    // 每个字段对应一对 get/set 方法，供JSON序列化和MyBatis映射使用

    /** 获取主键ID */
    public Integer getId() {
        return id; // 返回当前对象的id值
    }

    /** 设置主键ID */
    public void setId(Integer id) {
        this.id = id; // 将参数id赋值给当前对象的id字段
    }

    /** 获取用户名 */
    public String getUsername() {
        return username;
    }

    /** 设置用户名 */
    public void setUsername(String username) {
        this.username = username;
    }

    /** 获取昵称 */
    public String getName() {
        return name;
    }

    /** 设置昵称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取密码 */
    public String getPassword() {
        return password;
    }

    /** 设置密码 */
    public void setPassword(String password) {
        this.password = password;
    }

    /** 获取角色标识 */
    public String getRole() {
        return role;
    }

    /** 设置角色标识 */
    public void setRole(String role) {
        this.role = role;
    }

    /** 获取新密码（修改密码时使用） */
    public String getNewPassword() {
        return newPassword;
    }

    /** 设置新密码 */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /** 获取头像URL */
    public String getAvatar() {
        return avatar;
    }

    /** 设置头像URL */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /** 获取手机号 */
    public String getPhone() {
        return phone;
    }

    /** 设置手机号 */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** 获取验证码 */
    public String getCaptcha() {
        return captcha;
    }

    /** 设置验证码 */
    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

}
