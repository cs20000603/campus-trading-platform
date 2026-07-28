package com.wll.common.websocket; // WebSocket相关公共类

/**
 * WebSocket推送事件类型枚举
 * 系统定义了8种可以通过WebSocket实时推送给客户端的事件
 * 前端根据eventType判断通知类型，做不同的UI展示
 */
public enum WebSocketEventType {
    ORDER_NEW,      // 新订单通知（用户下单→推送给商家）
    ORDER_STATUS,   // 订单状态变更通知（商家接单/发货→推送给买家）
    SHOP_APPROVE,   // 店铺审核通过通知（管理员审核通过→推送给店主）
    SHOP_REJECT,    // 店铺审核拒绝通知（管理员驳回→推送给店主）
    SHOP_APPLY,     // 新店铺申请通知（用户申请开店→推送给管理员）
    IDLE_NEW,       // 新闲置商品发布通知（广播给闲置广场所有用户）
    IDLE_MESSAGE,   // 闲置商品新私信通知（有人发来私信→推送给接收者）
    IDLE_SOLD       // 闲置商品已售出通知（商品被购买→推送给卖家）
}
