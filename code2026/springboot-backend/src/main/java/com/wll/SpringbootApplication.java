// 声明该类所属的根包路径，com.wll是Spring Boot应用的最顶层包（Spring会自动扫描该包及其子包下的所有组件）
package com.wll;

// 导入MyBatis-Plus或MyBatis-Spring的@MapperScan注解，用于指定MyBatis Mapper接口所在的包路径，Spring会自动扫描并将这些接口注册为Bean
import org.mybatis.spring.annotation.MapperScan;
// 导入Spring Boot核心类SpringApplication，包含静态run方法，用于启动Spring Boot应用程序（创建ApplicationContext、自动配置、启动内嵌Tomcat等）
import org.springframework.boot.SpringApplication;
// 导入@SpringBootApplication注解，这是一个组合注解，等价于@SpringBootConfiguration + @EnableAutoConfiguration + @ComponentScan三个注解之和
import org.springframework.boot.autoconfigure.SpringBootApplication;
// 导入@ComponentScan注解，用于显式指定Spring组件扫描的包路径（覆盖默认的扫描范围）
import org.springframework.context.annotation.ComponentScan;

// @SpringBootApplication注解：Spring Boot应用的核心注解
// 包含三个子注解的功能：
// 1. @SpringBootConfiguration：标记该类为Spring Boot的配置类（类似@Configuration）
// 2. @EnableAutoConfiguration：启用Spring Boot的自动配置机制，根据类路径下的依赖自动配置项目（如DataSource、Tomcat、Jackson等）
// 3. @ComponentScan：自动扫描当前包及其子包下的Spring组件（@Component、@Service、@Controller、@Repository等）
@SpringBootApplication
// @ComponentScan注解：显式指定组件扫描的基础包路径
// basePackages = {"com.wll", "com.wll.common"}：扫描com.wll包和com.wll.common包下的所有Spring组件
// 通常不需要单独配置（@SpringBootApplication已默认扫描同包及子包），此处显式配置可能用于确保跨模块扫描
@ComponentScan(basePackages = {"com.wll", "com.wll.common"})
// @MapperScan注解：指定MyBatis Mapper接口所在的包路径
// "com.wll.common.mapper"：Spring启动时会扫描该包下的所有接口，通过JDK动态代理为每个Mapper接口生成代理实现类并注册为Bean
// 类似于在每个Mapper接口上单独添加@Mapper注解，但批量扫描更简洁
@MapperScan("com.wll.common.mapper")
// 声明SpringbootApplication公共类，该类是Spring Boot应用的入口点
public class SpringbootApplication {

    /**
     * Spring Boot应用程序的主入口方法（main方法）
     * JVM从该方法启动整个Spring Boot应用
     * @param args 命令行参数数组，可以在启动时通过命令行传入自定义参数（如 --server.port=8081）
     */
    // main方法：Java程序的入口点，JVM调用此方法启动应用程序
    // public static void：标准的Java主方法签名
    public static void main(String[] args) {
        // SpringApplication.run()：启动Spring Boot应用的核心方法
        // 参数1: SpringbootApplication.class - 指定主配置类（即当前类），Spring会从此类开始加载配置
        // 参数2: args - 命令行参数，可传入如--server.port=8080等运行时配置
        // run方法内部流程：1.创建ApplicationContext 2.触发自动配置 3.启动内嵌Tomcat 4.扫描并初始化所有Spring Bean 5.发布应用启动事件
        SpringApplication.run(SpringbootApplication.class, args);
    }

}
