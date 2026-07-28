// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.config;

// 导入JWT拦截器类，用于在HTTP请求到达Controller前进行JWT身份验证
import com.wll.miniapp.utils.JwtInterceptor;
// 导入Spring的Configuration注解，标记该类为配置类
import org.springframework.context.annotation.Configuration;
// 导入拦截器注册类，用于向Spring MVC注册拦截器
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
// 导入WebMvcConfigurer接口，用于自定义Spring MVC的配置
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 标记该类为Spring MVC配置类
@Configuration
// Spring MVC配置类，实现WebMvcConfigurer接口来定制拦截器等MVC组件
public class WebMvcConfig implements WebMvcConfigurer {

    // JWT拦截器实例，用于验证请求头中的JWT令牌（通过构造器注入）
    private final JwtInterceptor jwtInterceptor;

    // 构造器注入JWT拦截器依赖
    public WebMvcConfig(JwtInterceptor jwtInterceptor) {
        // 保存注入的JWT拦截器实例
        this.jwtInterceptor = jwtInterceptor;
    }

    // 重写WebMvcConfigurer接口的方法，用于添加自定义拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 向Spring MVC注册中心添加JWT拦截器
        registry.addInterceptor(jwtInterceptor)
                // 指定拦截器需要拦截的URL路径模式（只有这些路径需要登录验证）
                .addPathPatterns(
                        // 购物车相关接口需要登录
                        "/cart/**",
                        // 订单相关接口需要登录
                        "/orders/**",
                        // 收藏相关接口需要登录
                        "/collect/**",
                        // 充值相关接口需要登录
                        "/recharge/**",
                        // 用户相关接口需要登录
                        "/user/**",
                        // 文件上传下载接口需要登录
                        "/files/**",
                        // 修改密码接口需要登录
                        "/updatePassword",
                        // 绑定手机号接口需要登录
                        "/auth/bindPhone",
                        // 获取用户信息接口需要登录
                        "/auth/userInfo",
                        // 发表评论接口需要登录
                        "/comment/add",
                        // 修改评论接口需要登录
                        "/comment/update",
                        // 删除评论接口需要登录
                        "/comment/delete/**",
                        // 店铺注册接口需要登录
                        "/shop/register",
                        // 查看我的店铺接口需要登录
                        "/shop/my",
                        // 更新店铺信息接口需要登录
                        "/shop/update"
                )
                // 排除不需要拦截的路径（即使匹配了上面的规则也不拦截）
                .excludePathPatterns(
                        // 评论分页查询允许公开访问
                        "/comment/selectPage",
                        // 评论列表查询允许公开访问
                        "/comment/selectAll",
                        // 重置密码允许不登录访问（忘记密码场景）
                        "/auth/resetPassword"
                );
    }
}
