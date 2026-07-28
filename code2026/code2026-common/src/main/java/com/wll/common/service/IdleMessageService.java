// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的DateUtil类，提供日期时间工具方法（如now()获取当前时间字符串）
import cn.hutool.core.date.DateUtil;
// 导入IdleMessage实体类，对应数据库idle_message（闲置消息）表的ORM映射，
// 包含发送方ID(senderId)、接收方ID(receiverId)、闲置商品ID(idleId)、消息内容(content)、是否已读(isRead)等字段
import com.wll.common.entity.IdleMessage;
// 导入IdleMessageMapper数据访问接口，封装对idle_message表的所有数据库CRUD操作及自定义查询（对话查询、未读统计、标记已读）
import com.wll.common.mapper.IdleMessageMapper;
// 导入WebSocket事件类型枚举，定义IDLE_MESSAGE事件类型用于发送实时消息推送
import com.wll.common.websocket.WebSocketEventType;
// 导入WebSocket消息类，封装推送给客户端的消息体
import com.wll.common.websocket.WebSocketMessage;
// 导入WebSocket推送服务类，负责向指定用户实时推送消息
import com.wll.common.websocket.WebSocketPushService;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件
import org.springframework.stereotype.Service;
// 导入Spring的@Transactional注解，用于声明式事务管理
import org.springframework.transaction.annotation.Transactional;

// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;

/**
 * 闲置消息业务处理服务
 * 负责闲置交易中买卖双方私信消息的发送、对话查询、标记已读、未读统计等业务
 * 业务规则：
 * - 发送消息时自动初始化未读状态（isRead=0）和发送时间
 * - 发送消息后通过WebSocket实时推送给接收方（在线即时通知）
 * - 对话记录按时间顺序排列，用于页面展示聊天历史
 * - 标记已读支持按对话维度批量操作（将某一对话中指定发送方发的全部消息标记为已读）
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class IdleMessageService {

    // @Resource 注解：注入IdleMessageMapper Bean，用于执行idle_message表的数据库CRUD操作
    @Resource
    private IdleMessageMapper idleMessageMapper;
    // @Resource 注解：注入WebSocketPushService Bean，用于向接收方实时推送消息到达通知
    @Resource
    private WebSocketPushService wsPushService;

    /**
     * 发送闲置私信消息
     * 完整流程：初始化未读状态和时间 -> 插入数据库 -> WebSocket实时推送给接收方 -> 返回保存后的消息实体
     * 使用@Transactional保证数据库写入的原子性
     * @param msg 消息实体对象，必须包含发送方ID(senderId)、接收方ID(receiverId)、闲置商品ID(idleId)、消息内容(content)
     * @return 保存后的消息实体（包含数据库自动生成的id），调用方可据此获取消息ID
     */
    @Transactional  // 开启事务：保证insert操作的原子性
    public IdleMessage send(IdleMessage msg) {
        // 第一步：初始化消息为未读状态（isRead=0），等接收方查看后标记为1
        msg.setIsRead(0);
        // 第二步：记录消息发送时间为当前系统时间
        msg.setCreateTime(DateUtil.now());
        // 第三步：将消息插入到idle_message数据库表中，插入后msg对象会被MyBatis回填自增主键id
        idleMessageMapper.insert(msg);
        // 第四步：通过WebSocket向接收方实时推送消息通知
        // pushToUser 向指定用户ID的WebSocket连接发送消息（如果接收方在线则立即收到，离线则无法收到）
        // 消息内容使用原始content，后续可改为仅推送通知摘要（如"您有一条新消息"）以保护隐私
        wsPushService.pushToUser(msg.getReceiverId(),
            new WebSocketMessage(WebSocketEventType.IDLE_MESSAGE, msg.getReceiverId(),
                msg.getContent()));
        // 第五步：返回保存后的消息实体（已包含数据库自增ID，可供调用方后续使用）
        return msg;
    }

    /**
     * 获取两个用户关于某个闲置商品的完整对话记录
     * 查询两人之间针对指定闲置商品的所有消息，按时间顺序排列
     * 用于聊天页面加载历史消息记录
     * @param idleId 闲置商品ID，标识这是关于哪个闲置商品的对话
     * @param userId1 用户1的ID（通常是当前登录用户）
     * @param userId2 用户2的ID（对话的另一方）
     * @return 两个用户之间关于该闲置商品的所有消息列表，按发送时间升序排列（从早到晚）
     */
    public List<IdleMessage> getConversation(Integer idleId, Integer userId1, Integer userId2) {
        // 调用Mapper层自定义方法selectByConversation，执行查询：
        // SELECT * FROM idle_message WHERE idle_id=? AND ((sender_id=? AND receiver_id=?) OR (sender_id=? AND receiver_id=?)) ORDER BY create_time
        return idleMessageMapper.selectByConversation(idleId, userId1, userId2);
    }

    /**
     * 将指定对话中发送方发给接收方的所有消息批量标记为已读
     * 通常当接收方打开聊天页面查看消息时调用此方法
     * 使用@Transactional保证批量更新操作的原子性
     * @param idleId 闲置商品ID，指定对话所在的闲置商品
     * @param senderId 发送方用户ID
     * @param receiverId 接收方用户ID（即当前查看消息的用户）
     */
    @Transactional  // 开启事务：保证批量标记已读操作的原子性
    public void markAsRead(Integer idleId, Integer senderId, Integer receiverId) {
        // 调用Mapper层自定义方法markAsRead，执行SQL：
        // UPDATE idle_message SET is_read = 1 WHERE idle_id=? AND sender_id=? AND receiver_id=?
        idleMessageMapper.markAsRead(idleId, senderId, receiverId);
    }

    /**
     * 统计某用户的未读消息总数量
     * 用于在页面上显示消息红点提醒（如"您有3条未读消息"）
     * @param receiverId 接收方用户ID（通常为当前登录用户）
     * @return 该用户所有未读消息的总数（整数值）
     */
    public int countUnread(Integer receiverId) {
        // 调用Mapper层自定义方法countUnread，执行SQL：
        // SELECT COUNT(*) FROM idle_message WHERE receiver_id=? AND is_read=0
        return idleMessageMapper.countUnread(receiverId);
    }
}
