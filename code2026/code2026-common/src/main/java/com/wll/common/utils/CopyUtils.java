package com.wll.common.utils; // 声明包路径

import cn.hutool.core.io.FileUtil; // Hutool的文件工具，用于读写文件
import cn.hutool.core.util.StrUtil; // Hutool的字符串工具

import java.io.File; // Java的File类

/**
 * 代码自动生成工具（开发辅助，非业务代码）
 * 作用：根据已有模板（如Category），快速复制生成新功能模块的样板代码
 * 自动生成：Mapper接口、Mapper XML、Service、Controller、Vue页面
 *
 * 使用方法：修改main方法中的sourceName（模板名）和targetName（新名称），然后运行main方法
 * 示例：sourceName="Category", targetName="Comment"
 *   将自动生成 CommentMapper.java、CommentMapper.xml、CommentService.java、
 *   CommentController.java、Comment.vue
 *
 * 注意：路径中的package写死为com/example（与当前项目包路径com/wll不同），
 * 说明这个工具是从其他项目模板复制过来的，使用时需要调整路径
 */
public class CopyUtils {
    /** Java源码的包根路径 */
    private static String packagePath = System.getProperty("user.dir") + "/springboot-backend/src/main/java/com/example";
    /** MyBatis XML映射文件的资源路径 */
    private static String resourcesPath = System.getProperty("user.dir") + "/springboot-backend/src/main/resources";
    /** 管理后台Vue页面路径 */
    private static String managerVuePath = System.getProperty("user.dir") + "/manager-frontend/src/views";
    /** 用户前台Vue页面路径 */
    private static String userVuePath = System.getProperty("user.dir") + "/user-frontend/src/views";

    /** Controller文件后缀 */
    private static String controllerSuffix = "Controller.java";
    /** Service文件后缀 */
    private static String serviceSuffix = "Service.java";
    /** Mapper接口文件后缀 */
    private static String mapperSuffix = "Mapper.java";
    /** Mapper XML文件后缀 */
    private static String mapperXmlSuffix = "Mapper.xml";
    /** Vue文件后缀 */
    private static String vueSuffix = ".vue";

    /**
     * 程序入口，运行此main方法即可自动生成代码
     * @param args 命令行参数（未使用）
     */
    public static void main(String[] args) {
        String sourceName = "Category"; // 模板源文件名（要复制哪个已有的模块）
        String targetName = "Comment"; // 目标新文件名（要生成哪个新模块）

        copyMapperXml(sourceName, targetName); // 复制并替换XML映射文件
        copyMapper(sourceName, targetName); // 复制并替换Mapper接口
        copyService(sourceName, targetName); // 复制并替换Service服务类
        copyController(sourceName, targetName); // 复制并替换Controller控制器

        copyManagerVue(sourceName, targetName); // 复制并替换管理后台Vue页面
    }

    /**
     * 复制并替换Mapper XML映射文件
     * 读取模板XML文件内容，将所有sourceName替换为targetName后写入新文件
     * @param sourceName 模板名
     * @param targetName 新名称
     */
    public static void copyMapperXml(String sourceName, String targetName) {
        String mapperXmlPathPrefix = resourcesPath + "/mapper/"; // XML文件目录路径
        String content = FileUtil.readUtf8String(mapperXmlPathPrefix + sourceName + mapperXmlSuffix); // 读取模板文件
        String result1 = StrUtil.replace(content, sourceName, targetName); // 替换类名（首字母大写）
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName),
                StrUtil.lowerFirst(targetName)); // 替换变量名（首字母小写）

        File targetFile = FileUtil.touch(mapperXmlPathPrefix + targetName + mapperXmlSuffix); // 创建目标文件
        FileUtil.writeUtf8String(result, targetFile); // 写入替换后的内容
        System.out.println(targetName + mapperXmlSuffix + "复制成功！"); // 打印成功日志
    }

    /**
     * 复制并替换Mapper接口
     */
    public static void copyMapper(String sourceName, String targetName) {
        String mapperPathPrefix = packagePath + "/mapper/"; // Mapper接口目录
        String content = FileUtil.readUtf8String(mapperPathPrefix + sourceName + mapperSuffix); // 读取模板
        String result1 = StrUtil.replace(content, sourceName, targetName); // 替换类名
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName),
                StrUtil.lowerFirst(targetName)); // 替换变量名

        File targetFile = FileUtil.touch(mapperPathPrefix + targetName + mapperSuffix); // 创建目标文件
        FileUtil.writeUtf8String(result, targetFile); // 写入内容
        System.out.println(targetName + mapperSuffix + "复制成功！");
    }

    /**
     * 复制并替换Service服务类
     */
    public static void copyService(String sourceName, String targetName) {
        String servicePathPrefix = packagePath + "/service/"; // Service目录
        String content = FileUtil.readUtf8String(servicePathPrefix + sourceName + serviceSuffix); // 读取模板
        String result1 = StrUtil.replace(content, sourceName, targetName); // 替换类名
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName),
                StrUtil.lowerFirst(targetName)); // 替换变量名

        File targetFile = FileUtil.touch(servicePathPrefix + targetName + serviceSuffix); // 创建目标文件
        FileUtil.writeUtf8String(result, targetFile); // 写入内容
        System.out.println(targetName + serviceSuffix + "复制成功！");
    }

    /**
     * 复制并替换Controller控制器
     */
    public static void copyController(String sourceName, String targetName) {
        String controllerPathPrefix = packagePath + "/controller/"; // Controller目录
        String content = FileUtil.readUtf8String(controllerPathPrefix + sourceName + controllerSuffix); // 读取模板
        String result1 = StrUtil.replace(content, sourceName, targetName); // 替换类名
        String result = StrUtil.replace(result1, StrUtil.lowerFirst(sourceName),
                StrUtil.lowerFirst(targetName)); // 替换变量名

        File targetFile = FileUtil.touch(controllerPathPrefix + targetName + controllerSuffix); // 创建目标文件
        FileUtil.writeUtf8String(result, targetFile); // 写入内容
        System.out.println(targetName + controllerSuffix + "复制成功！");
    }

    /**
     * 复制并替换管理后台Vue页面
     * Vue页面的替换规则特殊：只需要替换URL路径中的小写部分
     */
    public static void copyManagerVue(String sourceName, String targetName) {
        String content = FileUtil.readUtf8String(managerVuePath + "/" + sourceName + vueSuffix); // 读取模板Vue文件
        String result = StrUtil.replace(content, "/" + StrUtil.lowerFirst(sourceName),
                "/" + StrUtil.lowerFirst(targetName)); // 替换路由路径中的小写部分

        File targetFile = FileUtil.touch(managerVuePath + "/" + targetName + vueSuffix); // 创建目标Vue文件
        FileUtil.writeUtf8String(result, targetFile); // 写入内容
        System.out.println("manager-" + targetName + vueSuffix + "复制成功！");
    }

    /**
     * 复制并替换用户前台Vue页面
     */
    public static void copyUserVue(String sourceName, String targetName) {
        String content = FileUtil.readUtf8String(userVuePath + "/" + sourceName + vueSuffix); // 读取模板
        String result = StrUtil.replace(content, "/" + StrUtil.lowerFirst(sourceName),
                "/" + StrUtil.lowerFirst(targetName)); // 替换路由路径

        File targetFile = FileUtil.touch(userVuePath + "/" + targetName + vueSuffix); // 创建目标文件
        FileUtil.writeUtf8String(result, targetFile); // 写入内容
        System.out.println("user-" + targetName + vueSuffix + "复制成功！");
    }

}
