// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.utils;

// 导入JJWT库的Claims接口，表示JWT的载荷数据（存储了userId、openid等自定义声明）
import io.jsonwebtoken.Claims;
// 导入JJWT库的Jwts类，用于构建和解析JWT令牌的核心入口
import io.jsonwebtoken.Jwts;
// 导入JJWT库的Keys工具类，用于创建HMAC-SHA签名密钥
import io.jsonwebtoken.security.Keys;
// 导入Spring的值注入注解，用于从配置文件中读取JWT相关配置
import org.springframework.beans.factory.annotation.Value;
// 导入Spring的Component注解，标记该类为Spring容器管理的组件
import org.springframework.stereotype.Component;

// 导入javax.crypto.SecretKey类，表示加密算法使用的密钥
import javax.crypto.SecretKey;
// 导入StandardCharsets类，指定字符编码为UTF-8
import java.nio.charset.StandardCharsets;
// 导入Date类，用于处理JWT的签发时间和过期时间
import java.util.Date;

// 标记该类为Spring组件，被Spring容器扫描和管理
@Component
// JWT工具类，提供JWT令牌的生成、验证和解析功能
public class JwtUtils {

    // 从Spring配置文件中读取jwt.secret配置项的值（JWT签名密钥），注入到secret字段
    @Value("${jwt.secret}")
    // JWT签名的密钥字符串，用于生成和验证令牌的HMAC签名
    private String secret;

    // 从Spring配置文件中读取jwt.expiration配置项的值（JWT过期时间，单位毫秒）
    @Value("${jwt.expiration}")
    // JWT令牌的有效期时长（毫秒），从签发时间开始计算
    private long expiration;

    // 私有方法，根据secret字符串生成HMAC-SHA签名密钥对象
    private SecretKey getKey() {
        // 使用secret字符串的UTF-8字节数组作为密钥材料，返回HMAC-SHA算法的密钥对象
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 生成JWT令牌的方法，将用户ID和openid作为自定义声明写入令牌
    public String generateToken(Integer userId, String openid) {
        // 获取当前时间作为令牌签发时间
        Date now = new Date();
        // 计算令牌过期时间 = 当前时间 + 配置的有效期时长
        Date expiryDate = new Date(now.getTime() + expiration);

        // 使用Jwts构建器创建JWT令牌
        return Jwts.builder()
                // 向令牌载荷中添加自定义声明：用户ID
                .claim("userId", userId)
                // 向令牌载荷中添加自定义声明：微信openid
                .claim("openid", openid)
                // 设置令牌的签发时间（iat声明）
                .issuedAt(now)
                // 设置令牌的过期时间（exp声明）
                .expiration(expiryDate)
                // 使用HMAC-SHA密钥对令牌进行签名
                .signWith(getKey())
                // 构建并返回紧凑格式的JWT字符串（由三段base64组成：header.payload.signature）
                .compact();
    }

    // 验证JWT令牌的方法，解析令牌并返回其载荷数据
    public Claims validateToken(String token) {
        // 使用Jwts解析器
        return Jwts.parser()
                // 设置验证签名所需的HMAC-SHA密钥
                .verifyWith(getKey())
                // 构建解析器
                .build()
                // 解析传入的令牌字符串，验证签名并提取签名后的声明
                .parseSignedClaims(token)
                // 获取并返回令牌的载荷（Claims对象，包含所有自定义声明）
                .getPayload();
    }

    // 从JWT令牌中提取用户ID的方法，先验证令牌有效性再获取载荷中的userId
    public Integer getUserId(String token) {
        // 调用验证方法解析令牌，获取Claims载荷对象
        Claims claims = validateToken(token);
        // 从载荷中提取"userId"自定义声明，类型为Integer，并返回
        return claims.get("userId", Integer.class);
    }
}
