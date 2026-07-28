// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.utils;

// 导入Hutool的JSONObject类，用于解析微信API返回的JSON格式响应数据
import cn.hutool.json.JSONObject;
// 导入Hutool的JSONUtil工具类，用于将字符串解析为JSON对象
import cn.hutool.json.JSONUtil;
// 导入微信配置类，获取微信小程序的AppID和AppSecret
import com.wll.miniapp.config.WechatConfig;
// 导入Spring的Component注解，标记该类为Spring容器管理的组件
import org.springframework.stereotype.Component;

// 导入URI类，用于构建HTTP请求的目标URL地址
import java.net.URI;
// 导入Java 11的HttpClient类，用于发送HTTP请求
import java.net.http.HttpClient;
// 导入HttpRequest类，用于构建HTTP请求（URL、方法、头部等）
import java.net.http.HttpRequest;
// 导入HttpResponse类，表示HTTP请求的响应结果
import java.net.http.HttpResponse;

// 标记该类为Spring组件，被Spring容器扫描和管理
@Component
// 微信API工具类，封装与微信服务器交互的HTTP请求逻辑
public class WechatUtils {

    // 微信配置实例，提供AppID和AppSecret等配置信息（通过构造器注入）
    private final WechatConfig wechatConfig;
    // Java 11的HttpClient实例，用于发送HTTP请求（线程安全，可复用）
    private final HttpClient httpClient;

    // 构造器注入微信配置依赖，并初始化HttpClient实例
    public WechatUtils(WechatConfig wechatConfig) {
        // 保存注入的微信配置实例
        this.wechatConfig = wechatConfig;
        // 创建默认配置的HttpClient实例（跟随重定向，使用系统代理等默认行为）
        this.httpClient = HttpClient.newHttpClient();
    }

    // 调用微信小程序登录接口，用临时授权码code换取用户的openid和session_key
    public JSONObject code2session(String code) {
        // 使用try-catch捕获HTTP请求和响应解析过程中可能发生的异常
        try {
            // 拼接微信API的请求URL，格式化字符串包含appid、secret、js_code和grant_type四个参数
            String url = String.format(
                    // 微信小程序登录接口URL模板
                    "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                    // 替换第一个参数：微信小程序的AppID
                    wechatConfig.getAppId(),
                    // 替换第二个参数：微信小程序的AppSecret
                    wechatConfig.getAppSecret(),
                    // 替换第三个参数：前端传来的临时登录授权码code
                    code
            );
            // 构建HTTP GET请求对象
            HttpRequest request = HttpRequest.newBuilder()
                    // 设置请求的目标URI地址
                    .uri(URI.create(url))
                    // 设置HTTP请求方法为GET
                    .GET()
                    // 构建请求对象
                    .build();
            // 发送HTTP请求并获取响应，指定响应体以字符串形式返回
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // 将响应的JSON字符串解析为JSONObject对象并返回
            return JSONUtil.parseObj(response.body());
        } catch (Exception e) {
            // HTTP请求或解析异常，包装为运行时异常并抛出
            throw new RuntimeException("微信登录请求失败", e);
        }
    }
}
