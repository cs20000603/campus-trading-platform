package com.wll.common.entity; // 声明包路径

import java.math.BigDecimal; // 导入BigDecimal精确金额
import java.util.List; // 导入List，用于嵌套购物车列表和订单详情列表

/**
 * 订单实体类
 * 对应数据库中的orders表，存储用户下单的主记录
 * 一个订单包含一个或多个商品（通过order_detail子表关联）
 * 状态字段记录了订单从创建到完成的整个生命周期
 */
public class Orders {

    /** 订单主键ID，数据库自增 */
    private Integer id;
    /** 订单编号，唯一标识，格式如"20260701143025a8f3"（日期+时间戳+随机数） */
    private String orderNo;
    /** 订单总金额（BigDecimal精确金额） */
    private BigDecimal total;
    /** 下单用户ID */
    private Integer userId;
    /** 下单用户名，冗余字段 */
    private String userName;
    /** 所属店铺ID（普通商城订单有值，闲置商品订单可为空） */
    private Integer shopId;
    /** 订单状态："待接单"→"已出货"→"已配送"→"已完成"，或"已取消" */
    private String status;
    /** 下单时间 */
    private String time;
    /** 配送方式，如"外卖配送"、"到店自取" */
    private String deliverType;
    /** 收货地址 */
    private String address;
    /** 配送员/配送信息 */
    private String deliver;
    /** 购物车列表（下单时的临时数据，不持久化到orders表），用于下单时传递商品信息 */
    private List<Cart> cartList;
    /** 订单详情列表（订单对应的每个商品条目），通过order_detail表关联查询 */
    private List<OrderDetail> orderDetailList;
    /** 商品名称汇总（闲置商品订单使用，方便列表展示） */
    private String goodsName;

    public String getGoodsName() { return goodsName; }
    public void setGoodsName(String goodsName) { this.goodsName = goodsName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getDeliver() { return deliver; }
    public void setDeliver(String deliver) { this.deliver = deliver; }

    public List<OrderDetail> getOrderDetailList() { return orderDetailList; }
    public void setOrderDetailList(List<OrderDetail> orderDetailList) { this.orderDetailList = orderDetailList; }

    public List<Cart> getCartList() { return cartList; }
    public void setCartList(List<Cart> cartList) { this.cartList = cartList; }

    public String getDeliverType() { return deliverType; }
    public void setDeliverType(String deliverType) { this.deliverType = deliverType; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getShopId() { return shopId; }
    public void setShopId(Integer shopId) { this.shopId = shopId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
