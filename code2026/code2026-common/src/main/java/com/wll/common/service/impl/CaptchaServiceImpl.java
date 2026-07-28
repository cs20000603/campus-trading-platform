// 声明当前类所属的包路径，com.wll.common.service.impl包下存放服务接口的实现类
package com.wll.common.service.impl;

// 导入CaptchaService接口，本类是该接口的Redis实现，实现管理员验证码生成与校验
import com.wll.common.service.CaptchaService;
// 导入Lombok的@Slf4j注解，编译时自动生成log静态字段（SLF4J日志门面），可直接用log.info/error等方法输出日志
import lombok.extern.slf4j.Slf4j;
// 导入Spring的@Autowired注解，用于按类型自动注入Spring容器中的Bean（与@Resource功能类似但匹配策略不同）
import org.springframework.beans.factory.annotation.Autowired;
// 导入Spring Data Redis的StringRedisTemplate类，专门操作字符串类型的Redis键值对（K-V均为String）
import org.springframework.data.redis.core.StringRedisTemplate;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件，由Spring容器管理
import org.springframework.stereotype.Service;

// 导入Java标准库的Random类，用于生成随机数（选取随机字符构成验证码）
import java.util.Random;
// 导入Java标准库的TimeUnit枚举，用于指定时间单位（此处用MINUTES表示分钟）
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务实现类
 * 基于Redis实现管理员验证码的生成、存储和校验
 * 验证码规则：
 * - 4位随机字符，从数字0-9和大写字母A-Z中随机选取（共36个候选字符，排除小写字母以避免混淆）
 * - 通过Redis存储，key格式为 admin:captcha:{username}
 * - 有效期5分钟，过期后Redis自动删除
 * - 校验后无论成功或失败都立即删除Redis中的验证码，保证一次性使用
 * - 生成时将验证码打印到控制台供管理员查看（开发演示用，生产环境应改为邮件/短信）
 * @Service 注解将本类注册为Spring容器中的单例Bean，bean名称默认首字母小写为captchaServiceImpl
 * @Slf4j 注解由Lombok在编译期生成 private static final org.slf4j.Logger log = ...; 可直接使用log输出日志
 */
@Service
@Slf4j
public class CaptchaServiceImpl implements CaptchaService {

    // @Autowired 注解：按类型自动注入Spring Data Redis提供的StringRedisTemplate Bean
    // StringRedisTemplate封装了Redis的SET/GET/DELETE等字符串操作，value和key均为String类型
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 定义Redis中验证码存储的key前缀常量，拼接用户名后完整key格式为 admin:captcha:{username}
    private static final String CAPTCHA_PREFIX = "admin:captcha:";
    // 定义验证码过期时间常量，5分钟后Redis自动删除该key（TimeUnit.MINUTES配合使用）
    private static final int CAPTCHA_EXPIRE_MINUTES = 5;
    // 定义验证码长度常量，生成的验证码由4个随机字符组成
    private static final int CAPTCHA_LENGTH = 4;

    /**
     * 为指定管理员生成验证码
     * 完整流程：调用随机生成方法生成4位验证码 -> 输出到控制台 -> 存储到Redis并设置过期时间 -> 返回验证码明文
     * @param username 管理员用户名，用于构建Redis key（admin:captcha:{username}）
     * @return 生成的4位随机验证码字符串（如"3F8K"）
     */
    @Override
    public String generateAdminCaptcha(String username) {
        // 第一步：调用私有方法generateRandomCaptcha随机生成4位验证码
        String captcha = generateRandomCaptcha();

        // 第二步：将验证码打印输出到服务器控制台，供管理员在开发/演示环境中查看
        // 生产环境下应改为通过邮件或短信渠道发送验证码
        System.out.println("=====================================");
        System.out.println("管理员验证码（用户名: " + username + "）: " + captcha);
        System.out.println("=====================================");

        // 第三步：将验证码存储到Redis中，key为 admin:captcha:{username}，value为验证码明文
        // opsForValue().set(key, value, timeout, timeUnit) 设置字符串类型的键值对
        // 设置过期时间为5分钟，到期后Redis自动删除该key（无需手动清理过期数据）
        stringRedisTemplate.opsForValue().set(
            CAPTCHA_PREFIX + username,  // Redis key：admin:captcha:用户名
            captcha,                     // Redis value：验证码明文
            CAPTCHA_EXPIRE_MINUTES,      // 过期时长：5
            TimeUnit.MINUTES             // 时间单位：分钟（即5分钟后过期）
        );

        // 第四步：返回生成的验证码明文给调用方
        return captcha;
    }

    /**
     * 校验管理员输入的验证码是否正确
     * 完整流程：从Redis读取验证码 -> 忽略大小写比对 -> 立即删除Redis中的验证码 -> 返回校验结果
     * 验证码校验后无论成功与否都会被删除，保证每个验证码只能使用一次（防止重放攻击）
     * @param username 管理员用户名，用于定位Redis中该用户的验证码
     * @param captcha 管理员在登录界面输入的验证码字符串
     * @return true表示验证码存在且匹配成功（忽略大小写）；false表示验证码不存在（已过期/未生成）或不匹配
     */
    @Override
    public boolean verifyAdminCaptcha(String username, String captcha) {
        // 第一步：拼接Redis key
        String key = CAPTCHA_PREFIX + username;
        // 第二步：从Redis获取存储的验证码（如果key不存在返回null，如果已过期Redis也已自动删除返回null）
        String storedCaptcha = stringRedisTemplate.opsForValue().get(key);

        // 第三步：如果Redis中取出的验证码为null，说明验证码不存在（未生成或已过期），直接返回false
        if (storedCaptcha == null) {
            return false;
        }

        // 第四步：忽略大小写比较Redis中存储的验证码与管理员的输入是否一致
        // equalsIgnoreCase 会将两字符串统一转为大写（或小写）后比较，用户无需关心大小写
        boolean isValid = storedCaptcha.equalsIgnoreCase(captcha);

        // 第五步：验证后立即删除Redis中的验证码key
        // 无论比对成功或失败都删除，保证验证码一次性有效，防止被恶意重放
        stringRedisTemplate.delete(key);

        // 第六步：返回验证结果（true=验证通过，false=验证失败）
        return isValid;
    }

    /**
     * 生成随机验证码（私有方法，仅供本类内部调用）
     * 从字符集"0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"中随机选取4个字符拼接成验证码
     * 字符集包含数字0-9（共10个）和大写字母A-Z（共26个），总计36个候选字符
     * 排除小写字母是为了避免大小写混淆（如字母O与数字0、字母I与数字1等）
     * @return 4位随机验证码字符串，每个字符都是数字或大写字母
     */
    private String generateRandomCaptcha() {
        // 第一步：定义可用字符集 —— 数字0-9 加上大写字母A-Z，共36个字符
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        // 第二步：创建StringBuilder用于高效拼接字符（避免String频繁创建新对象）
        StringBuilder sb = new StringBuilder();
        // 第三步：创建Random实例作为随机数生成器
        Random random = new Random();
        // 第四步：循环4次，每次从字符集中随机选取一个字符追加到StringBuilder中
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            // random.nextInt(chars.length()) 生成 [0, 36) 范围内的随机整数作为索引
            // chars.charAt(索引) 取出该索引对应的字符（数字或大写字母）
            // sb.append(字符) 将字符追加到StringBuilder末尾
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        // 第五步：将StringBuilder转为String并返回
        return sb.toString();
    }
}
