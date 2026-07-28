// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的StrUtil类，提供字符串判空、判非空等工具方法（如isBlank、isNotBlank）
import cn.hutool.core.util.StrUtil;
// 导入Hutool的HttpRequest类，用于构建和发送HTTP请求（支持GET/POST，设置header、body、超时等）
import cn.hutool.http.HttpRequest;
// 导入Hutool的HttpResponse类，封装HTTP响应结果（包含status状态码、body响应体等）
import cn.hutool.http.HttpResponse;
// 导入Hutool的JSONArray类，表示JSON数组结构，用于构建DeepSeek API请求体中的messages数组
import cn.hutool.json.JSONArray;
// 导入Hutool的JSONObject类，表示JSON对象结构，用于构建请求体及解析API返回的JSON响应
import cn.hutool.json.JSONObject;
// 导入Hutool的JSONUtil类，提供JSON字符串解析（parseObj）等JSON操作工具方法
import cn.hutool.json.JSONUtil;
// 导入Spring的@Value注解，用于从application.yml/properties配置文件中将配置值注入到字段
import org.springframework.beans.factory.annotation.Value;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件，由Spring容器管理生命周期
import org.springframework.stereotype.Service;

// 导入ArrayList类，用于从JSON解析结果中提取字符串列表时创建可变列表
import java.util.ArrayList;
// 导入HashMap类，用于创建键值对结构的集合容器
import java.util.HashMap;
// 导入List接口，用于接收JSON解析后的列表数据
import java.util.List;
// 导入Map接口，用于承载JSON解析后的键值对数据
import java.util.Map;

/**
 * AI智能服务
 * 集成DeepSeek大语言模型API（https://api.deepseek.com），提供三大AI能力：
 * 1. 智能客服聊天（chat）：以"校园小卖部平台智能客服助手"角色解答购物相关问题
 * 2. 商品描述生成（generateDescription）：根据商品名称和分类自动生成50-100字营销文案
 * 3. 智能搜索关键词提取（smartSearch）：将用户自然语言转为1-3个核心搜索关键词
 * API密钥和地址通过@Value从配置文件注入，未配置时各方法返回友好提示而不会崩溃
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class AiService {

    // @Value 注解：从Spring配置文件（application.yml/application.properties）中读取deepseek.api-key配置项的值
    // ${deepseek.api-key:} 中的冒号后为空字符串，表示如果配置项不存在则默认值为空字符串（避免启动报错）
    @Value("${deepseek.api-key:}")
    private String apiKey;

    // @Value 注解：从配置文件读取DeepSeek API的基础URL地址，默认为官方API地址 https://api.deepseek.com
    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    // 定义使用的DeepSeek模型名称常量，deepseek-chat为DeepSeek的通用对话模型
    private static final String MODEL = "deepseek-chat";

    /**
     * 通用聊天接口（智能客服）
     * 构建"校园小卖部平台智能客服助手"系统提示词，调用DeepSeek API回答用户购物相关问题
     * @param userMessage 用户输入的自然语言消息字符串
     * @return AI助手生成的回复文本内容；如果API密钥未配置则返回"AI 服务未配置（缺少 API Key）"提示
     */
    public String chat(String userMessage) {
        // 第一步：检查API密钥是否已配置，未配置则直接返回友好提示（避免NullPointerException）
        if (StrUtil.isBlank(apiKey)) {
            return "AI 服务未配置（缺少 API Key）";
        }
        // 第二步：构建系统提示词，定义AI的角色身份和行为约束（校园小卖部客服、回答简洁、范围限于校园购物）
        String systemPrompt = "你是校园小卖部平台的智能客服助手，帮助用户解答购物、订单、配送等问题。" +
                "回答简洁友好，不超过200字。如果问题超出校园购物范围，礼貌告知用户。";
        // 第三步：调用底层通用方法callDeepSeek，传入系统提示词和用户消息，发起API请求并返回结果
        return callDeepSeek(systemPrompt, userMessage);
    }

    /**
     * 生成商品描述文案
     * 以电商文案专家的角色，根据商品名称和分类自动生成50-100字吸引人的商品描述
     * @param goodsName 商品名称字符串，作为描述生成的主要依据
     * @param category 商品分类名称，可为null（为null时在提示词中显示"未指定"）
     * @return 生成的商品描述文本；如果API密钥未配置则返回"AI 服务未配置（缺少 API Key）"提示
     */
    public String generateDescription(String goodsName, String category) {
        // 第一步：检查API密钥是否已配置
        if (StrUtil.isBlank(apiKey)) {
            return "AI 服务未配置（缺少 API Key）";
        }
        // 第二步：构建系统提示词，定义AI为电商商品文案专家角色，要求生成50-100字吸引人的描述
        String systemPrompt = "你是电商商品文案专家。根据商品名称和分类，生成一段吸引人的商品描述，50-100字。";
        // 第三步：构建用户提示词，拼接商品名称和分类信息（分类为null时显示"未指定"）
        String userPrompt = "商品名称：" + goodsName + "\n分类：" + (category != null ? category : "未指定") +
                "\n请生成商品描述：";
        // 第四步：调用底层通用方法callDeepSeek，传入系统提示词和用户提示词，发起API请求
        return callDeepSeek(systemPrompt, userPrompt);
    }

    /**
     * 智能搜索：将自然语言购物需求转为核心搜索关键词
     * 例如用户输入"我想买一个便宜的蓝牙耳机" -> AI提取返回"蓝牙耳机 便宜"
     * @param query 用户输入的自然语言搜索需求字符串
     * @return 提取的核心搜索关键词（1-3个，空格分隔），未配置API密钥时返回null（调用方可据此判断降级处理）
     */
    public String smartSearch(String query) {
        // 第一步：检查API密钥是否已配置，未配置返回null让调用方进行降级处理
        if (StrUtil.isBlank(apiKey)) {
            return null;
        }
        // 第二步：构建系统提示词，要求AI将自然语言转为1-3个核心搜索关键词，仅输出关键词
        String systemPrompt = "将用户的自然语言购物需求，提取为1-3个核心搜索关键词，用空格分隔。只输出关键词，不要其他内容。";
        // 第三步：调用底层通用方法callDeepSeek，传入系统提示词和用户的自然语言查询
        return callDeepSeek(systemPrompt, query);
    }

    /**
     * 调用DeepSeek API的底层通用方法（私有方法，仅供本类内部调用）
     * 完整的API调用流程：构建JSON请求体 -> 发送HTTP POST请求 -> 解析JSON响应 -> 提取AI回复内容
     * @param systemPrompt 系统提示词字符串，用于定义AI的角色身份和任务指令
     * @param userMessage 用户消息字符串，即用户的具体问题或需求
     * @return API返回的AI文本回复内容（已去除首尾空白）；网络异常或API错误时返回包含错误信息的字符串
     */
    private String callDeepSeek(String systemPrompt, String userMessage) {
        try {
            // === 第一步：构建请求体JSON对象 ===
            // 创建根JSON对象作为请求体
            JSONObject body = new JSONObject();
            // 设置模型名称（deepseek-chat）
            body.set("model", MODEL);
            // 设置最大生成token数为600（限制回复长度以控制成本）
            body.set("max_tokens", 600);

            // 创建messages数组，DeepSeek API的消息格式为 [{"role":"system","content":"..."}, {"role":"user","content":"..."}]
            JSONArray messages = new JSONArray();

            // 添加系统提示消息：role为"system"表示这是对AI行为的指令约束
            JSONObject sysMsg = new JSONObject();
            sysMsg.set("role", "system");
            sysMsg.set("content", systemPrompt);
            messages.add(sysMsg);

            // 添加用户消息：role为"user"表示这是用户的提问内容
            JSONObject userMsg = new JSONObject();
            userMsg.set("role", "user");
            userMsg.set("content", userMessage);
            messages.add(userMsg);

            // 将messages数组放入请求体
            body.set("messages", messages);

            // === 第二步：发送HTTP POST请求到DeepSeek API ===
            // 拼接完整的API端点URL（Chat Completions接口）
            String url = baseUrl + "/v1/chat/completions";
            // 使用Hutool的HttpRequest构建POST请求：
            // - header设置Bearer Token认证（使用配置的apiKey）
            // - header设置Content-Type为application/json
            // - body设置JSON请求体字符串
            // - timeout设置30秒超时（避免长时间等待）
            // - execute()执行请求并获取响应
            HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(body.toString())
                    .timeout(30000)
                    .execute();

            // === 第三步：解析响应，提取AI回复内容 ===
            // 检查HTTP状态码是否为200（成功）
            if (response.getStatus() == 200) {
                // 将响应体JSON字符串解析为JSONObject
                JSONObject result = JSONUtil.parseObj(response.body());
                // 从响应中提取choices数组（DeepSeek返回的候选回复列表）
                JSONArray choices = result.getJSONArray("choices");
                // 确认choices不为空且至少有一个元素
                if (choices != null && !choices.isEmpty()) {
                    // 取第一个choice对象（通常只有一个）
                    JSONObject choice = choices.getJSONObject(0);
                    // 从choice中提取message对象
                    JSONObject message = choice.getJSONObject("message");
                    // 从message中提取content字段（即AI的文本回复），去除首尾空白后返回
                    return message.getStr("content", "").trim();
                }
            }
            // HTTP状态码非200，返回包含状态码的错误提示
            return "AI 请求失败：" + response.getStatus();
        } catch (Exception e) {
            // 捕获所有异常（网络异常、JSON解析异常等），打印堆栈以便排查问题
            e.printStackTrace();
            // 返回包含异常信息的友好提示给调用方
            return "AI 服务异常：" + e.getMessage();
        }
    }
}
