package com.wll.common.entity; // 声明包路径

/**
 * 搜索日志实体类
 * 对应数据库中的search_log表，记录用户的搜索历史
 * 可用于分析用户搜索偏好、热门搜索关键词统计等
 */
public class SearchLog {

    /** 搜索日志主键ID，数据库自增 */
    private Integer id;
    /** 搜索用户的ID */
    private Integer userId;
    /** 搜索用户名，冗余字段 */
    private String userName;
    /** 用户输入的搜索关键词 */
    private String keyword;
    /** 搜索时间 */
    private String time;

    /** 获取日志ID */
    public Integer getId() {
        return id;
    }

    /** 设置日志ID */
    public void setId(Integer id) {
        this.id = id;
    }

    /** 获取搜索用户ID */
    public Integer getUserId() {
        return userId;
    }

    /** 设置搜索用户ID */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /** 获取搜索用户名 */
    public String getUserName() {
        return userName;
    }

    /** 设置搜索用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取搜索关键词 */
    public String getKeyword() {
        return keyword;
    }

    /** 设置搜索关键词 */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /** 获取搜索时间 */
    public String getTime() {
        return time;
    }

    /** 设置搜索时间 */
    public void setTime(String time) {
        this.time = time;
    }
}
