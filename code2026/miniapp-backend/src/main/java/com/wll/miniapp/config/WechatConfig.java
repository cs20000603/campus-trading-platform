// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.config;

// 导入Lombok的@Data注解，自动生成getter、setter、toString、equals、hashCode方法
import lombok.Data;
// 导入Spring Boot的配置属性绑定注解，将配置文件中的wechat前缀属性绑定到本类
import org.springframework.boot.context.properties.ConfigurationProperties;
// 导入Spring的Component注解，标记该类为Spring容器管理的组件
import org.springframework.stereotype.Component;

// 标记该类为Spring组件，被Spring容器扫描和管理
@Component
// 将application.yml/properties中以"wechat"为前缀的配置项绑定到本类的属性上
@ConfigurationProperties(prefix = "wechat")
// Lombok注解，自动生成所有字段的getter和setter方法
@Data
// 微信配置类，用于存储微信小程序的AppID和AppSecret等配置信息
public class WechatConfig {
    // 微信小程序的AppID（应用唯一标识），从配置文件中wechat.app-id读取
    private String appId;
    // 微信小程序的AppSecret（应用密钥），从配置文件中wechat.app-secret读取
    private String appSecret;
}
