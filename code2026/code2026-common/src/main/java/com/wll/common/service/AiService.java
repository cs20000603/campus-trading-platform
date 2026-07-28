package com.wll.common.service;

import cn.hutool.core.util.StrUtil;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AiService {

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    private static final String MODEL = "deepseek-chat";

    private ChatLanguageModel model;

    @PostConstruct
    public void init() {
        if (StrUtil.isNotBlank(apiKey)) {
            model = OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .baseUrl(baseUrl + "/v1")
                    .modelName(MODEL)
                    .maxTokens(600)
                    .timeout(Duration.ofSeconds(30))
                    .build();
        }
    }

    public String chat(String userMessage) {
        if (model == null) {
            return "AI 服务未配置（缺少 API Key）";
        }
        String systemPrompt = "你是校园小卖部平台的智能客服助手，帮助用户解答购物、订单、配送等问题。" +
                "回答简洁友好，不超过200字。如果问题超出校园购物范围，礼貌告知用户。";
        try {
            return model.generate(SystemMessage.from(systemPrompt), UserMessage.from(userMessage))
                    .content().text().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 服务异常：" + e.getMessage();
        }
    }

    public String generateDescription(String goodsName, String category) {
        if (model == null) {
            return "AI 服务未配置（缺少 API Key）";
        }
        String systemPrompt = "你是电商商品文案专家。根据商品名称和分类，生成一段吸引人的商品描述，50-100字。";
        String userPrompt = "商品名称：" + goodsName + "\n分类：" + (category != null ? category : "未指定") +
                "\n请生成商品描述：";
        try {
            return model.generate(SystemMessage.from(systemPrompt), UserMessage.from(userPrompt))
                    .content().text().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "AI 服务异常：" + e.getMessage();
        }
    }

    public String smartSearch(String query) {
        if (model == null) {
            return null;
        }
        String systemPrompt = "将用户的自然语言购物需求，提取为1-3个核心搜索关键词，用空格分隔。只输出关键词，不要其他内容。";
        try {
            return model.generate(SystemMessage.from(systemPrompt), UserMessage.from(query))
                    .content().text().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
