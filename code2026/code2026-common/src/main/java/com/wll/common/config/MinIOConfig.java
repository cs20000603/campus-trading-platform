package com.wll.common.config; // 声明包路径

import io.minio.MinioClient; // MinIO客户端类，用于与MinIO服务器交互
import org.springframework.beans.factory.annotation.Value; // @Value注解，从application.yml读取配置值
import org.springframework.context.annotation.Bean; // @Bean注解
import org.springframework.context.annotation.Configuration; // @Configuration注解

/**
 * MinIO对象存储配置类
 * 创建并配置MinioClient Bean，供FileController等类注入使用
 *
 * MinIO是什么？
 * 一个开源的对象存储服务器，兼容亚马逊S3云存储的API协议
 * 可以自建部署，用来存储图片、视频、文档等任意文件
 * 支持通过HTTP URL直接访问存储的文件，适合做电商系统的图片服务器
 *
 * 三个配置项从application.yml中读取：
 * minio.endpoint   - MinIO服务地址（如 http://127.0.0.1:9000）
 * minio.access-key - 访问密钥
 * minio.secret-key - 秘密密钥
 */
@Configuration // 标识这是一个Spring配置类
public class MinIOConfig {

    /** MinIO服务地址，从yml配置读取，如http://127.0.0.1:9000 */
    @Value("${minio.endpoint}") // 从配置文件读取minio.endpoint的值，注入到endpoint字段
    private String endpoint;

    /** MinIO访问密钥，类似登录用户名 */
    @Value("${minio.access-key}")
    private String accessKey;

    /** MinIO秘密密钥，类似登录密码 */
    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * 创建MinioClient Bean
     * 使用Builder模式设置endpoint和认证凭据
     * @return 配置好的MinioClient实例，注册到Spring容器中
     */
    @Bean // 将返回的MinioClient注册为Spring Bean
    public MinioClient minioClient() {
        return MinioClient.builder() // 使用建造者模式创建MinioClient
                .endpoint(endpoint) // 设置MinIO服务器地址
                .credentials(accessKey, secretKey) // 设置访问密钥和秘密密钥（用于身份验证）
                .build(); // 构建MinioClient实例
    }
}
