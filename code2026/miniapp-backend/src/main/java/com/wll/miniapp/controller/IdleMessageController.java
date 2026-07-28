// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入闲置消息实体类，映射数据库中的闲置商品聊天消息表
import com.wll.common.entity.IdleMessage;
// 导入闲置消息服务接口，提供消息发送、查询、标记已读等操作
import com.wll.common.service.IdleMessageService;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入List集合类，用于存储查询到的消息列表
import java.util.List;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /idleMessage
@RequestMapping("/idleMessage")
// 闲置消息控制器，处理闲置商品聊天中消息的发送、获取会话、标记已读、未读计数等请求
public class IdleMessageController {

    // 通过@Resource注解注入闲置消息服务实例（按名称装配）
    @Resource
    // 闲置消息服务接口引用，用于调用消息相关业务逻辑
    private IdleMessageService idleMessageService;

    // 映射POST请求到 /idleMessage/send，发送一条闲置商品聊天消息
    @PostMapping("/send")
    // @RequestBody将请求体JSON绑定到IdleMessage实体，HttpServletRequest获取当前用户ID作为发送者
    public Result send(@RequestBody IdleMessage msg, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 设置消息的发送者ID为当前登录用户
        msg.setSenderId(userId);
        // 调用消息服务发送消息（保存到数据库）
        idleMessageService.send(msg);
        // 将发送成功后的消息对象（含数据库生成的ID等）包装为成功结果返回
        return Result.success(msg);
    }

    // 映射GET请求到 /idleMessage/conversation，获取两个用户关于某个闲置商品的对话记录
    @GetMapping("/conversation")
    // @RequestParam获取闲置商品ID
    public Result conversation(@RequestParam Integer idleId,
                               // @RequestParam获取对话对方的用户ID
                               @RequestParam Integer otherId,
                               // HttpServletRequest获取当前用户ID
                               HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 调用消息服务获取两个用户关于指定闲置商品的完整对话记录
        List<IdleMessage> list = idleMessageService.getConversation(idleId, userId, otherId);
        // 将对话中对方发给当前用户的未读消息标记为已读
        idleMessageService.markAsRead(idleId, otherId, userId);
        // 将对话记录列表包装为成功结果返回
        return Result.success(list);
    }

    // 映射PUT请求到 /idleMessage/read，标记某条消息为已读
    @PutMapping("/read")
    // @RequestBody将请求体JSON绑定到IdleMessage实体，HttpServletRequest获取当前用户ID
    public Result markAsRead(@RequestBody IdleMessage msg, HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 调用消息服务：将发送者发给当前用户的关于指定闲置商品的消息标记为已读
        idleMessageService.markAsRead(msg.getIdleId(), msg.getSenderId(), userId);
        // 返回操作成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /idleMessage/unreadCount，获取当前用户的未读消息总数
    @GetMapping("/unreadCount")
    // HttpServletRequest获取当前用户ID
    public Result unreadCount(HttpServletRequest request) {
        // 从请求属性中获取当前登录用户ID
        Integer userId = (Integer) request.getAttribute("userId");
        // 调用消息服务统计当前用户的未读消息数量
        int count = idleMessageService.countUnread(userId);
        // 将未读消息数量包装为成功结果返回
        return Result.success(count);
    }
}
