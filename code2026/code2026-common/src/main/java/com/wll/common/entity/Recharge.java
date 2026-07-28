package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal

/**
 * 充值记录实体类
 * 对应数据库中的recharge表，记录用户的账户充值历史
 * 用户在前端选择充值金额和支付方式后，后端创建充值记录并增加用户余额
 */
public class Recharge {

    /** 充值记录主键ID，数据库自增 */
    private Integer id;
    /** 充值金额（BigDecimal精确金额） */
    private BigDecimal money;
    /** 充值用户ID */
    private Integer userId;
    /** 充值用户名，冗余字段 */
    private String userName;
    /** 支付方式/充值类型，如"微信支付"、"支付宝"、"银行卡"等 */
    private String type;
    /** 充值时间 */
    private String time;

    /** 获取充值记录ID */
    public Integer getId() {
        return id;
    }

    /** 设置充值记录ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取充值金额 */
    public BigDecimal getMoney() {
        return money;
    }

    /** 设置充值金额 */
    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    /** 获取充值用户ID */
    public Integer getUserId() {
        return userId;
    }

    /** 设置充值用户ID */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /** 获取充值用户名 */
    public String getUserName() {
        return userName;
    }

    /** 设置充值用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取支付方式 */
    public String getType() {
        return type;
    }

    /** 设置支付方式 */
    public void setType(String type) {
        this.type = type;
    }

    /** 获取充值时间 */
    public String getTime() {
        return time;
    }

    /** 设置充值时间 */
    public void setTime(String time) {
        this.time = time;
    }
}
