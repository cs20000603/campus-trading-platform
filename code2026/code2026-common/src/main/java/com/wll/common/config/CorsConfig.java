package com.wll.common.config; // 声明包路径

import org.springframework.context.annotation.Bean; // @Bean注解
import org.springframework.context.annotation.Configuration; // @Configuration注解
import org.springframework.web.cors.CorsConfiguration; // CORS配置类，用于设置跨域规则
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // 基于URL路径的CORS配置源
import org.springframework.web.filter.CorsFilter; // CORS过滤器，拦截所有请求并添加跨域响应头

/**
 * 全局跨域（CORS）配置类
 * 解决前后端分离部署时的跨域问题
 *
 * 什么是跨域？
 * 浏览器的同源策略会阻止一个域名下的网页请求另一个域名的API
 * 比如前端部署在 http://localhost:5173，后端API在 http://localhost:9090
 * 浏览器会默认拦截这种跨域请求
 * CORS（Cross-Origin Resource Sharing）通过在响应头中添加特殊标记，
 * 告诉浏览器"我允许来自其他域名的请求"
 */
@Configuration // 标识这是一个配置类
public class CorsConfig {

    /**
     * 创建CorsFilter Bean
     * 配置允许所有来源、所有请求头、所有HTTP方法的跨域访问
     * @return CorsFilter实例
     */
    @Bean // 注册CorsFilter到Spring容器
    public CorsFilter corsFilter() {
        // 创建基于URL路径的配置源（可以对不同路径设置不同的跨域策略）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration(); // 创建CORS配置对象
        corsConfiguration.addAllowedOriginPattern("*"); // 允许所有来源域名（*是通配符）
        corsConfiguration.addAllowedHeader("*"); // 允许所有请求头（如Authorization、Content-Type等）
        corsConfiguration.addAllowedMethod("*"); // 允许所有HTTP方法（GET/POST/PUT/DELETE/OPTIONS等）
        corsConfiguration.setAllowCredentials(true); // 允许携带Cookie/认证信息（前后端分离时需要）
        source.registerCorsConfiguration("/**", corsConfiguration); // 将上述规则应用到所有路径

        return new CorsFilter(source); // 创建并返回CorsFilter
    }
}
