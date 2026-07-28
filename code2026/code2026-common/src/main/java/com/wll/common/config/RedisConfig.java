package com.wll.common.config; // 声明包路径

import org.springframework.context.annotation.Bean; // 导入@Bean注解，标记方法返回的对象由Spring容器管理
import org.springframework.context.annotation.Configuration; // 导入@Configuration注解，标识这是一个配置类
import org.springframework.data.redis.connection.RedisConnectionFactory; // Redis连接工厂，自动注入
import org.springframework.data.redis.core.RedisTemplate; // Redis操作模板，核心API
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer; // JSON序列化器
import org.springframework.data.redis.serializer.StringRedisSerializer; // 字符串序列化器

/**
 * Redis配置类
 * 自定义RedisTemplate的序列化方式
 * 为什么要自定义？Spring Boot默认使用JDK序列化，存入Redis的数据是二进制不可读的
 * 这里改为：Key用String序列化，Value用JSON序列化，存入Redis的数据是人类可读的JSON格式
 * 好处：调试方便、跨语言兼容、减少存储空间
 *
 * Redis（Remote Dictionary Server）是什么？
 * 一个基于内存的键值对存储数据库，读写速度极快（每秒10万+次），常用于缓存、验证码存储、分布式锁等场景
 * 本项目中Redis用于存储管理员登录验证码，设置5分钟自动过期
 */
@Configuration // 告诉Spring这是一个配置类，里面的@Bean方法会被调用并将返回对象加入容器
public class RedisConfig {

    /**
     * 创建自定义的RedisTemplate Bean
     * Spring Boot自动注入了RedisConnectionFactory（连接工厂），用于创建与Redis服务器的连接
     * @param redisConnectionFactory Spring自动注入的Redis连接工厂
     * @return 配置好的RedisTemplate实例
     */
    @Bean // 将返回的RedisTemplate对象注册为Spring Bean，供其他类通过@Autowired注入使用
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>(); // 创建RedisTemplate实例
        template.setConnectionFactory(redisConnectionFactory); // 设置Redis连接工厂

        // ---- Key的序列化设置 ----
        // 设置Key的序列化器为StringRedisSerializer（将Key转成UTF-8字符串存到Redis）
        template.setKeySerializer(new StringRedisSerializer()); // 普通Key用String序列化
        template.setHashKeySerializer(new StringRedisSerializer()); // Hash结构的Key也用String序列化

        // ---- Value的序列化设置 ----
        // 设置Value的序列化器为GenericJackson2JsonRedisSerializer（将对象转成JSON字符串存到Redis）
        // 比如存入User对象时：{"id":1,"username":"admin","password":"123",...}，可读且跨语言
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // 普通Value用JSON序列化
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer()); // Hash结构的Value也用JSON序列化

        template.afterPropertiesSet(); // 初始化模板（调用此方法完成内部设置）
        return template; // 返回配置好的RedisTemplate
    }
}
