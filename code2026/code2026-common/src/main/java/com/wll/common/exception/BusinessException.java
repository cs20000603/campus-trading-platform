package com.wll.common.exception; // 声明包路径

import cn.hutool.core.map.MapUtil; // Hutool的Map构建工具
import cn.hutool.core.util.StrUtil; // Hutool的字符串工具
import cn.hutool.http.HttpResponse; // Hutool的HTTP响应对象
import cn.hutool.http.HttpUtil; // Hutool的HTTP请求工具
import cn.hutool.json.JSONUtil; // Hutool的JSON解析工具
import jakarta.annotation.PostConstruct; // @PostConstruct，在Bean初始化后自动执行
import jakarta.annotation.Resource; // @Resource，依赖注入

import org.springframework.context.ApplicationContext; // Spring应用上下文
import org.springframework.context.ConfigurableApplicationContext; // 可配置的应用上下文（可关闭）
import org.springframework.stereotype.Component; // @Component，标识该类为Spring组件

import java.io.BufferedReader; // 读取字符输入流
import java.io.InputStreamReader; // 将字节流转换为字符流
import java.util.Map; // Java的Map接口

/**
 * 软件授权检查组件（非业务异常）
 * 在Spring启动时自动执行，读取当前机器的UUID（唯一硬件标识），
 * 发送到远程授权服务器验证是否已购买授权。
 * 如果验证失败，关闭Spring容器并退出JVM进程。
 *
 * 注意：这个类虽然放在exception包中，但它不是真正的异常类，
 * 而是利用@PostConstruct机制在启动时执行授权检查的组件
 */
@Component // 注册为Spring组件，随容器启动而初始化
public class BusinessException extends RuntimeException {

    /** Spring应用上下文，用于在验证失败时关闭容器 */
    @Resource // 注入ApplicationContext
    ApplicationContext context;

    /** 订单号/授权编号，硬编码的固定值 */
    private static final String orderNo = "20424178839841013764";
    /** 验证类型 */
    private static final String type = "CODE";

    /**
     * 初始化方法，在Spring容器创建完Bean后、应用启动完成前自动执行
     * 无论成功或失败都捕获异常，避免授权检查本身的错误导致应用无法启动
     */
    @PostConstruct // 标识此方法在Bean构造完成、依赖注入完成后自动调用
    public void init() {
        try {
            String machineCode = getMachineCode(); // 获取当前机器的UUID
            judge(machineCode); // 发送到远程服务器验证授权
        } catch (Exception e) {
            // 授权检查过程中发生任何异常都静默处理，不影响启动
        }
    }

    /**
     * 授权判定方法
     * 向远程授权服务器发送HTTP请求，验证当前机器是否有合法授权
     * @param machineCode 当前机器的唯一标识码
     */
    private void judge(String machineCode) {
        if (StrUtil.isBlank(machineCode)) { // 如果无法获取机器码
            return; // 直接返回，不阻止应用启动
        }
        try {
            // 构建请求参数Map：机器码、订单号、验证类型
            Map<String, Object> map = MapUtil.<String, Object>builder()
                    .put("machineCode", machineCode) // 本机的UUID
                    .put("orderNo", orderNo) // 固定的授权编号
                    .put("type", type) // 验证类型
                    .build();
            // 发送GET请求到授权验证API，携带表单参数，30秒超时
            HttpResponse httpResponse = HttpUtil.createGet("https://api.javaxmsz.cn/orders/sourceCodeCheck")
                    .form(map) // 设置表单参数
                    .timeout(30000) // 设置超时30秒
                    .execute(); // 执行请求
            int status = httpResponse.getStatus(); // 获取HTTP响应状态码
            if (status != 200) { // 如果返回的不是200
                exit(); // 授权失败，退出应用
                return; // 退出前的return
            }
            // 解析响应JSON，检查code字段是否为"200"
            String code = JSONUtil.parseObj(httpResponse.body()).getStr("code"); // 从JSON中取code字段
            if (!"200".equals(code)) { // 如果code不等于"200"
                exit(); // 授权验证不通过，退出应用
            }
        } catch (Exception e) {
            // 网络异常等错误静默处理
        }

    }

    /**
     * 关闭Spring容器并退出JVM
     * 调用ConfigurableApplicationContext.close()优雅关闭，
     * 然后System.exit(0)强制终止JVM进程
     */
    private void exit() {
        ((ConfigurableApplicationContext) context).close(); // 先关闭Spring容器（释放资源）
        System.exit(0); // 然后退出JVM进程，0表示正常退出
    }

    /**
     * 获取当前机器的唯一标识码（UUID）
     * 根据操作系统执行不同的命令来获取硬件UUID
     * Windows: 执行 wmic csproduct get uuid
     * Linux:   执行 dmidecode -s system-uuid
     * Mac:     执行 system_profiler SPHardwareDataType
     * @return 机器的UUID字符串，获取失败返回"UNKNOWN"
     */
    public static String getMachineCode() {
        try {
            String os = System.getProperty("os.name").toLowerCase(); // 获取操作系统名称并转小写
            String command; // 要执行的命令
            if (os.contains("win")) { // Windows系统
                command = "wmic csproduct get uuid"; // WMIC命令获取UUID
            } else if (os.contains("linux")) { // Linux系统
                command = "dmidecode -s system-uuid | tr 'A-Z' 'a-z'"; // dmidecode命令获取UUID
            } else if (os.contains("mac")) { // Mac系统
                command = "system_profiler SPHardwareDataType |grep \"r (system)\""; // 获取硬件UUID
            } else {
                throw new UnsupportedOperationException("Unsupported OS"); // 不支持的操作系统抛出异常
            }
            Process process = Runtime.getRuntime().exec(command); // 执行系统命令，返回Process对象
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())); // 读取命令输出流
            String line;
            StringBuilder output = new StringBuilder(); // 累积命令输出
            while ((line = reader.readLine()) != null) { // 逐行读取输出
                output.append(line).append("\n"); // 每行追加换行符
            }
            return parseSerial(output.toString(), os); // 从命令行输出中解析出UUID
        } catch (Exception e) {
            return "UNKNOWN"; // 获取失败返回固定值
        }
    }

    /**
     * 从操作系统命令的输出中解析出UUID序列号
     * 不同操作系统的输出格式不同，需要不同的解析策略
     * @param output 操作系统命令的原始输出文本
     * @param os 操作系统名称
     * @return 解析后的UUID字符串
     */
    private static String parseSerial(String output, String os) {
        if (os.contains("win")) { // Windows输出格式：UUID\nxxxx-xxxx-xxxx-xxxx\n
            return output.replaceAll("UUID", "") // 去掉"UUID"标签
                    .replaceAll("\n", "") // 去掉所有换行符
                    .trim(); // 去掉首尾空格
        } else if (os.contains("linux")) { // Linux输出格式带有"ID:"前缀
            return output.replaceAll(".*ID:\\s+", "").trim(); // 去掉"ID:"前缀和前面的内容
        } else if (os.contains("mac")) { // Mac输出直接trim
            return output.trim(); // 去掉首尾空格
        }
        return "UNKNOWN"; // 无法识别格式，返回默认值
    }

}
