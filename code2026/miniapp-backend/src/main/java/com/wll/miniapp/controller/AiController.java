// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入AI服务接口，提供聊天、智能搜索、描述生成等AI功能
import com.wll.common.service.AiService;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 导入Map集合类，用于接收前端传递的键值对参数
import java.util.Map;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /ai
@RequestMapping("/ai")
// AI功能控制器，处理AI聊天、智能搜索、商品描述生成等请求
public class AiController {

    // 通过@Resource注解注入AI服务实例（按名称装配）
    @Resource
    // AI服务接口引用，用于调用AI相关业务逻辑
    private AiService aiService;

    // 映射POST请求到 /ai/chat，处理用户与AI的聊天对话
    @PostMapping("/chat")
    // @RequestBody将请求体中的JSON数据绑定到Map<String,String>参数
    public Result chat(@RequestBody Map<String, String> params) {
        // 从请求参数中获取用户输入的消息内容
        String message = params.get("message");
        // 校验消息是否为空或仅包含空白字符
        if (message == null || message.trim().isEmpty()) {
            // 如果消息为空，返回错误提示
            return Result.error("请输入问题");
        }
        // 调用AI服务进行聊天，去除消息首尾空白后传入
        String reply = aiService.chat(message.trim());
        // 将AI回复内容包装为成功结果返回
        return Result.success(reply);
    }

    // 映射POST请求到 /ai/smartSearch，智能分析用户搜索意图并提取关键词
    @PostMapping("/smartSearch")
    // @RequestBody将请求体中的JSON数据绑定到Map参数
    public Result smartSearch(@RequestBody Map<String, String> params) {
        // 从请求参数中获取用户的自然语言搜索查询
        String query = params.get("query");
        // 校验搜索查询是否为空或仅包含空白字符
        if (query == null || query.trim().isEmpty()) {
            // 如果查询为空，返回错误提示
            return Result.error("请输入搜索内容");
        }
        // 调用AI服务进行智能搜索解析，去除首尾空白后传入
        String keywords = aiService.smartSearch(query.trim());
        // 将提取的关键词包装为成功结果返回
        return Result.success(keywords);
    }

    // 映射POST请求到 /ai/generateDesc，根据商品名称和分类自动生成商品描述
    @PostMapping("/generateDesc")
    // @RequestBody将请求体中的JSON数据绑定到Map参数
    public Result generateDesc(@RequestBody Map<String, String> params) {
        // 从请求参数中获取商品名称
        String name = params.get("name");
        // 从请求参数中获取商品分类（可选参数）
        String category = params.get("category");
        // 校验商品名称是否为空或仅包含空白字符
        if (name == null || name.trim().isEmpty()) {
            // 如果商品名称为空，返回错误提示
            return Result.error("请输入商品名称");
        }
        // 调用AI服务生成商品描述，传入清理后的名称和分类信息
        String desc = aiService.generateDescription(name.trim(), category);
        // 将生成的描述文本包装为成功结果返回
        return Result.success(desc);
    }
}
