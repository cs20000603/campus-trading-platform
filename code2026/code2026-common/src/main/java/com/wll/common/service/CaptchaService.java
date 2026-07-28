// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务接口及其实现
package com.wll.common.service;

/**
 * 验证码服务接口
 * 定义管理员验证码的生成和校验行为规范，所有验证码实现类必须遵循此契约
 * 验证码的生命周期：生成 -> 存储（如Redis）-> 展示给管理员 -> 管理员输入 -> 校验 -> 立即删除（一次性使用）
 * 使用场景：管理员登录时需额外输入验证码，增强后台账户安全性
 */
public interface CaptchaService {

    /**
     * 为指定管理员生成验证码
     * 生成的验证码需存储到缓存中间件（如Redis）中并设置有效期（通常5分钟），
     * 同时将验证码输出到服务器控制台供管理员查看（开发/演示环境），
     * 生产环境应改为发送邮件或短信等安全方式
     * @param username 管理员用户名，用于标识该验证码归属于哪个管理员（作为缓存key的一部分）
     * @return 生成的验证码明文字符串（如"3F8K"），由数字和大写字母组成
     */
    String generateAdminCaptcha(String username);

    /**
     * 校验管理员输入的验证码是否正确
     * 从缓存中取出对应用户名的验证码，与管理员输入的验证码进行比对（通常忽略大小写）
     * 验证成功或失败后都必须立即删除缓存中的验证码，保证验证码一次性有效，防止重放攻击
     * @param username 管理员用户名，用于定位缓存中该用户的验证码
     * @param captcha 管理员输入的验证码字符串
     * @return true表示验证码存在且匹配成功；false表示验证码不存在（已过期/未生成）或不匹配
     */
    boolean verifyAdminCaptcha(String username, String captcha);
}
