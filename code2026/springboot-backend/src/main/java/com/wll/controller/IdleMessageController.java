// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入闲置消息实体类IdleMessage，对应数据库中的消息表，包含闲置商品ID、发送者ID、接收者ID、消息内容、已读状态等字段
import com.wll.common.entity.IdleMessage;
// 导入用户实体类User，用于从Session中获取当前登录用户信息
import com.wll.common.entity.User;
// 导入闲置消息服务接口IdleMessageService，封装消息发送、对话查询、已读标记、未读计数等业务逻辑
import com.wll.common.service.IdleMessageService;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入HttpSession，用于获取服务器端会话中的登录用户信息
import jakarta.servlet.http.HttpSession;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@PostMapping、@GetMapping、@PutMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回消息列表
import java.util.List;

/**
 * 闲置商品消息控制器
 * 处理闲置商品买卖双方的私信聊天功能，包括发送消息、查看对话、标记已读、未读计数
 * 请求路径前缀：/idleMessage
 */
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/idleMessage前缀
@RequestMapping("/idleMessage")
// 声明IdleMessageController公共类
public class IdleMessageController {

    // @Resource注解：按名称注入IdleMessageService Bean，负责闲置商品私信消息的业务逻辑
    @Resource
    private IdleMessageService idleMessageService;

    /**
     * 发送消息给卖家/买家
     * 请求方式：POST /idleMessage/send
     * 需要登录，自动将当前用户设为消息发送者
     * @param msg 消息实体对象（JSON请求体），包含闲置商品ID、接收者ID、消息内容
     * @param session HTTP会话，用于获取当前登录用户
     * @return Result 包含保存后的消息对象的成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/idleMessage/send
    @PostMapping("/send")
    // send方法：发送一条私信消息给指定接收者
    public Result send(@RequestBody IdleMessage msg,
                       // HttpSession参数：获取当前登录用户作为消息发送者
                       HttpSession session) {
        // 从Session中获取当前登录用户
        User user = (User) session.getAttribute("user");
        // 未登录不能发送消息
        if (user == null) return Result.error("请先登录");
        // 自动设置消息发送者ID为当前登录用户的ID，防止伪造发送者
        msg.setSenderId(user.getId());
        // 调用服务层保存消息到数据库（包含消息内容和关联的闲置商品ID）
        idleMessageService.send(msg);
        // 返回保存后的消息对象（含数据库生成的消息ID、发送时间等）
        return Result.success(msg);
    }

    /**
     * 获取与某个用户关于某个闲置商品的对话记录
     * 请求方式：GET /idleMessage/conversation?idleId=xxx&otherId=xxx
     * 需要登录，查询后自动将对方发来的消息标记为已读
     * @param idleId 闲置商品ID（URL查询参数），用于定位具体的闲置商品对话
     * @param otherId 对话对方的用户ID（URL查询参数）
     * @param session HTTP会话，用于获取当前登录用户
     * @return Result 包含按时间排序的消息列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/idleMessage/conversation
    @GetMapping("/conversation")
    // conversation方法：查询当前用户与指定对方关于指定闲置商品的聊天记录
    public Result conversation(@RequestParam Integer idleId,      // 闲置商品ID，必填
                               @RequestParam Integer otherId,     // 对方用户ID，必填
                               HttpSession session) {
        // 验证登录状态
        User user = (User) session.getAttribute("user");
        if (user == null) return Result.error("请先登录");
        // 查询当前用户(user.getId())与对方(otherId)关于该闲置商品(idleId)的所有消息，按时间正序排列
        List<IdleMessage> list = idleMessageService.getConversation(idleId, user.getId(), otherId);
        // 查询到对话记录后，自动将对方发来的消息标记为已读（isRead=true）
        // otherId是发送者，user.getId()是接收者，表示将otherId发给当前用户的消息标为已读
        idleMessageService.markAsRead(idleId, otherId, user.getId());
        // 返回对话消息列表
        return Result.success(list);
    }

    /**
     * 标记消息为已读
     * 请求方式：PUT /idleMessage/read
     * 将指定对话中对方发来的消息全部标记为已读状态
     * @param msg 消息实体对象（JSON请求体），需包含idleId和senderId（对方ID）
     * @param session HTTP会话，用于获取当前登录用户作为接收者
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，请求路径为/idleMessage/read
    @PutMapping("/read")
    // markAsRead方法：将指定对方发送给当前用户的消息全部标记为已读
    public Result markAsRead(@RequestBody IdleMessage msg, HttpSession session) {
        // 验证登录状态
        User user = (User) session.getAttribute("user");
        if (user == null) return Result.error("请先登录");
        // 将对方(msg.getSenderId())发给当前用户(user.getId())关于指定商品(msg.getIdleId())的消息标记为已读
        idleMessageService.markAsRead(msg.getIdleId(), msg.getSenderId(), user.getId());
        // 返回成功响应
        return Result.success();
    }

    /**
     * 获取当前用户未读消息数量
     * 请求方式：GET /idleMessage/unreadCount
     * 用于前端展示未读消息提醒小红点
     * @param session HTTP会话，用于获取当前登录用户
     * @return Result 包含未读消息整数的成功响应，未登录时返回0
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/idleMessage/unreadCount
    @GetMapping("/unreadCount")
    // unreadCount方法：获取当前用户的未读私信消息总数
    public Result unreadCount(HttpSession session) {
        // 从Session获取当前用户
        User user = (User) session.getAttribute("user");
        // 未登录时直接返回0条未读消息（不需要错误提示，未登录显示0即可）
        if (user == null) return Result.success(0);
        // 统计当前用户作为接收者且isRead=false的消息总数
        int count = idleMessageService.countUnread(user.getId());
        // 返回未读消息数量（如5条未读）
        return Result.success(count);
    }
}
