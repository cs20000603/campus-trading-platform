package com.wll.common.entity; // 声明包路径

/**
 * 管理员实体类
 * 继承Account父类，获得id/username/password/name/avatar/role等所有基础字段
 * 对应数据库中的admin表，用于存储系统管理员的账号信息
 * 管理员可以管理用户、商品、订单、店铺审核等
 */
public class Admin extends Account { // extends Account 表示继承父类的所有属性和方法

    /** 管理员主键ID，覆盖父类的id字段 */
    private Integer id;
    /** 管理员登录用户名，覆盖父类的username字段 */
    private String username;
    /** 管理员登录密码，覆盖父类的password字段 */
    private String password;
    /** 管理员真实姓名，覆盖父类的name字段 */
    private String name;
    /** 管理员头像URL，覆盖父类的avatar字段 */
    private String avatar;
    /** 管理员角色，默认为"管理员"，覆盖父类的role字段 */
    private String role;

    // ===== 以下为手动编写的 Getter/Setter 方法（@Override表示覆盖父类方法） =====

    @Override // 覆盖父类Account的getId方法
    public Integer getId() {
        return id; // 返回管理员ID
    }

    @Override // 覆盖父类Account的setId方法
    public void setId(Integer id) {
        this.id = id; // 设置管理员ID
    }

    @Override // 覆盖父类Account的getUsername方法
    public String getUsername() {
        return username; // 返回管理员用户名
    }

    @Override // 覆盖父类Account的setUsername方法
    public void setUsername(String username) {
        this.username = username; // 设置管理员用户名
    }

    @Override // 覆盖父类Account的getPassword方法
    public String getPassword() {
        return password; // 返回管理员密码
    }

    @Override // 覆盖父类Account的setPassword方法
    public void setPassword(String password) {
        this.password = password; // 设置管理员密码
    }

    @Override // 覆盖父类Account的getName方法
    public String getName() {
        return name; // 返回管理员姓名
    }

    @Override // 覆盖父类Account的setName方法
    public void setName(String name) {
        this.name = name; // 设置管理员姓名
    }

    @Override // 覆盖父类Account的getAvatar方法
    public String getAvatar() {
        return avatar; // 返回管理员头像URL
    }

    @Override // 覆盖父类Account的setAvatar方法
    public void setAvatar(String avatar) {
        this.avatar = avatar; // 设置管理员头像URL
    }

    @Override // 覆盖父类Account的getRole方法
    public String getRole() {
        return role; // 返回管理员角色
    }

    @Override // 覆盖父类Account的setRole方法
    public void setRole(String role) {
        this.role = role; // 设置管理员角色
    }
}
