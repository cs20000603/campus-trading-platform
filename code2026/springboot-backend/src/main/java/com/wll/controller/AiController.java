// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入AI服务接口AiService，封装AI对话、商品描述生成、智能搜索等AI相关业务逻辑
import com.wll.common.service.AiService;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@PostMapping、@RequestBody等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的Map接口，用于接收JSON格式的请求参数键值对
import java.util.Map;

/**
 * AI智能助手控制器
 * 提供AI对话、商品描述生成、智能搜索等AI相关功能
 * 请求路径前缀：/ai
 */
// @RestController注解：标记该类为RESTful控制器，所有方法返回值自动序列化为JSON并写入HTTP响应体
@RestController
// @RequestMapping注解：将该控制器所有接口的请求路径统一映射到/ai前缀下
@RequestMapping("/ai")
// 声明AiController公共类，继承Object基类
public class AiController {

    // @Resource注解：按名称注入AiService Bean，aiService负责调用大语言模型API进行AI对话、描述生成、智能搜索
    @Resource
    private AiService aiService;

    /**
     * AI智能对话接口
     * 请求方式：POST /ai/chat
     * 接收用户输入的问题，调用AI服务返回智能回复
     * @param params JSON请求体，包含message字段：{"message": "用户的问题"}
     * @return Result 包含AI回复文本的成功响应，或参数为空时的错误提示
     */
    // @PostMapping注解：将HTTP POST请求映射到该方法，请求路径为/ai/chat
    @PostMapping("/chat")
    // chat方法：接收包含message字段的JSON请求体Map，返回AI生成的对话回复
    // @RequestBody注解：将HTTP请求体中的JSON数据反序列化为Map<String, String>对象
    public Result chat(@RequestBody Map<String, String> params) {
        // 从请求体中提取用户消息文本（Map.get("message")获取message字段的值）
        String message = params.get("message");
        // 校验消息不能为空或全是空白字符，空白字符串无意义
        if (message == null || message.trim().isEmpty()) {
            // 返回错误响应（status=500, message="请输入问题"），提示用户输入有效问题
            return Result.error("请输入问题");
        }
        // 调用AI服务获取智能回复，传入去除首尾空格后的消息文本
        String reply = aiService.chat(message.trim());
        // 返回包含AI回复文本的成功响应
        return Result.success(reply);
    }

    /**
     * AI生成商品描述接口
     * 请求方式：POST /ai/generateDesc
     * 根据商品名称和分类，利用AI自动生成一段吸引人的商品描述文案
     * @param params JSON请求体，包含name（商品名称）和category（商品分类）字段
     * @return Result 包含AI生成的商品描述文本的成功响应
     */
    // @PostMapping注解：请求路径为/ai/generateDesc
    @PostMapping("/generateDesc")
    // generateDesc方法：接收商品名称和分类，通过AI生成商品描述文案
    public Result generateDesc(@RequestBody Map<String, String> params) {
        // 从请求体中提取商品名称（name字段）
        String name = params.get("name");
        // 从请求体中提取商品分类（category字段）
        String category = params.get("category");
        // 校验商品名称不能为空，名称是生成描述的必要输入
        if (name == null || name.trim().isEmpty()) {
            // 提示用户输入商品名称
            return Result.error("请输入商品名称");
        }
        // 调用AI服务根据商品名称和分类自动生成一段吸引人的描述文案
        String desc = aiService.generateDescription(name.trim(), category);
        // 返回包含AI生成描述文本的成功响应
        return Result.success(desc);
    }

    /**
     * AI智能搜索接口
     * 请求方式：POST /ai/smartSearch
     * 将用户的自然语言搜索语句转换为精准的搜索关键词列表，
     * 支持语义理解，如"200元以内的蓝牙耳机"会提取为多个精准关键词
     * @param params JSON请求体，包含query字段：{"query": "用户搜索的自然语言"}
     * @return Result 包含AI提取的关键词的成功响应
     */
    // @PostMapping注解：请求路径为/ai/smartSearch
    @PostMapping("/smartSearch")
    // smartSearch方法：接收用户的自然语言搜索语句，返回AI提取的精准搜索关键词
    public Result smartSearch(@RequestBody Map<String, String> params) {
        // 从请求体中提取用户的自然语言搜索语句（query字段）
        String query = params.get("query");
        // 校验搜索内容不能为空
        if (query == null || query.trim().isEmpty()) {
            // 提示用户输入搜索内容
            return Result.error("请输入搜索内容");
        }
        // 调用AI服务将自然语言搜索语句转换为精准的搜索关键词
        String keywords = aiService.smartSearch(query.trim());
        // 返回包含AI提取的关键词的成功响应
        return Result.success(keywords);
    }
}
