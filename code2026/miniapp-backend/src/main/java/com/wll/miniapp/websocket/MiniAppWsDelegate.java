// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.websocket;

// 导入公共模块的WebSocket推送委托接口，定义了向指定用户推送消息的契约
import com.wll.common.websocket.WebSocketPushDelegate;
// 导入公共模块的WebSocket推送服务类，管理所有推送委托的注册和调用
import com.wll.common.websocket.WebSocketPushService;
// 导入Jakarta的PostConstruct注解，标记在依赖注入完成后需要执行的初始化方法
import jakarta.annotation.PostConstruct;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入Spring的Component注解，标记该类为Spring容器管理的组件
import org.springframework.stereotype.Component;

// 标记该类为Spring组件，被Spring容器扫描和管理
@Component
// 小程序WebSocket推送委托类，实现WebSocketPushDelegate接口
// 作为桥梁连接公共模块的推送服务和本模块的具体WebSocket实现
public class MiniAppWsDelegate implements WebSocketPushDelegate {

    // 通过@Resource注解注入WebSocket推送服务实例（按名称装配）
    @Resource
    // WebSocket推送服务引用，管理所有推送委托的注册
    private WebSocketPushService wsPushService;

    // 使用@PostConstruct注解标记，在Spring容器完成依赖注入后自动调用此初始化方法
    @PostConstruct
    public void init() {
        // 将当前委托实例注册到推送服务中，使公共模块能够通过本委托向小程序用户推送消息
        wsPushService.registerDelegate(this);
    }

    // 重写WebSocketPushDelegate接口的方法，实现向指定用户推送JSON格式消息
    @Override
    public void pushToUser(Integer userId, String jsonMessage) {
        // 调用MiniAppWsHandler的静态方法，向指定用户ID的WebSocket会话发送消息
        MiniAppWsHandler.sendToUser(userId, jsonMessage);
    }
}
