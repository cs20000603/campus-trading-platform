// 声明当前类所属的包路径，作为Spring Boot应用的基础包路径
package com.wll.miniapp;

// 导入MyBatis-Plus的MapperScan注解，用于指定需要扫描的Mapper接口包路径
import org.mybatis.spring.annotation.MapperScan;
// 导入Spring Boot的SpringApplication类，用于启动Spring Boot应用
import org.springframework.boot.SpringApplication;
// 导入Spring Boot的SpringBootApplication注解，标记该类为Spring Boot主启动类
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 导入Spring的ComponentScan注解，用于指定需要扫描Spring组件的包路径
import org.springframework.context.annotation.ComponentScan;

// @SpringBootApplication是一个组合注解，包含以下功能：
// @EnableAutoConfiguration：启用Spring Boot的自动配置机制
// @ComponentScan：启用组件扫描（默认扫描当前包及其子包）
// @Configuration：标记该类为Java配置类
@SpringBootApplication
// 自定义组件扫描范围，除了本模块的包，还扫描公共模块com.wll.common下的组件
// basePackages指定多个包路径，使Spring能够发现并注册这些包下的所有@Component、@Service、@Controller等注解的Bean
@ComponentScan(basePackages = {"com.wll.miniapp", "com.wll.common"})
// 配置MyBatis的Mapper接口扫描路径
// 自动扫描指定包下的所有Mapper接口，并生成对应的代理实现类注入Spring容器
// 包括本模块的mapper和公共模块的mapper
@MapperScan({"com.wll.miniapp.mapper", "com.wll.common.mapper"})
// 小程序后端Spring Boot应用的主启动类
public class MiniappApplication {

    // Java应用程序的入口main方法，Spring Boot应用从这里启动
    public static void main(String[] args) {
        // 调用SpringApplication.run()启动Spring Boot应用
        // 第一个参数：主启动类的Class对象，作为应用配置的入口来源
        // 第二个参数：命令行参数，传递给Spring Boot应用
        // 该方法返回ApplicationContext对象，此处省略接收
        SpringApplication.run(MiniappApplication.class, args);
    }

}
