// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入MinIO客户端相关类，用于与MinIO对象存储服务交互
import io.minio.*;
// 导入MinIO HTTP方法枚举，用于生成预签名URL时指定HTTP方法
import io.minio.http.Method;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletResponse，用于控制HTTP响应（如下载重定向）
import jakarta.servlet.http.HttpServletResponse;
// 导入Spring的值注入注解，用于从配置文件中读取MinIO相关配置
import org.springframework.beans.factory.annotation.Value;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;
// 导入MultipartFile接口，用于接收前端上传的文件数据
import org.springframework.web.multipart.MultipartFile;

// 导入UUID工具类，用于生成唯一的文件名，防止文件名冲突
import java.util.UUID;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /files
@RequestMapping("/files")
// 文件控制器，处理文件上传到MinIO对象存储和文件下载等请求
public class FileController {

    // 通过@Resource注解注入MinIO客户端实例（按名称装配）
    @Resource
    // MinIO客户端对象，用于执行所有与MinIO服务器的交互操作
    private MinioClient minioClient;

    // 从Spring配置文件中读取minio.bucket-name配置项的值，注入到bucketName字段
    @Value("${minio.bucket-name}")
    // MinIO存储桶名称，所有上传的文件都存储在此桶中
    private String bucketName;

    // 从Spring配置文件中读取minio.endpoint配置项的值，注入到minioEndpoint字段
    @Value("${minio.endpoint}")
    // MinIO服务器访问端点地址，用于拼接文件访问URL
    private String minioEndpoint;

    // 映射POST请求到 /files/upload，处理文件上传
    @PostMapping("/upload")
    // MultipartFile接收前端上传的文件数据
    public Result upload(MultipartFile file) {
        // 使用try-catch捕获文件上传过程中可能发生的异常
        try {
            // 检查目标存储桶是否已存在
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            // 如果存储桶不存在
            if (!bucketExists) {
                // 创建新的存储桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            // 设置存储桶的访问策略为公开读（允许匿名用户通过URL直接访问文件）
            // 构建JSON格式的IAM策略，允许所有主体（*）对桶内所有对象执行GetObject操作
            String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
            // 将策略应用到存储桶上
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
            // 获取上传文件的原始文件名
            String originalFilename = file.getOriginalFilename();
            // 提取文件扩展名：如果原始文件名不为空且包含点号，则截取最后一个点号之后的字符串
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    // 如果无法提取扩展名，则使用空字符串
                    : "";
            // 使用UUID生成唯一文件名，拼接原文件的扩展名，防止文件名冲突
            String fileName = UUID.randomUUID().toString() + extension;
            // 执行文件上传到MinIO服务器
            minioClient.putObject(
                    // 构建上传参数
                    PutObjectArgs.builder()
                            // 指定目标存储桶名称
                            .bucket(bucketName)
                            // 指定存储后的对象名称（即文件名）
                            .object(fileName)
                            // 设置文件输入流、文件大小，-1表示不指定分片大小
                            .stream(file.getInputStream(), file.getSize(), -1)
                            // 设置文件的Content-Type（MIME类型）
                            .contentType(file.getContentType())
                            // 构建参数对象
                            .build()
            );
            // 拼接文件的公开访问URL：端点 + "/" + 桶名 + "/" + 文件名
            String url = minioEndpoint + "/" + bucketName + "/" + fileName;
            // 将文件访问URL包装为成功结果返回
            return Result.success(url);
        } catch (Exception e) {
            // 打印异常堆栈信息到控制台
            e.printStackTrace();
            // 返回文件上传失败的错误信息，包含具体异常消息
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    // 映射GET请求到 /files/download/{fileName}，处理文件下载（通过预签名URL重定向）
    @GetMapping("/download/{fileName}")
    // @PathVariable从URL路径中提取文件名，HttpServletResponse用于重定向响应
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        // 使用try-catch捕获下载过程中可能发生的异常
        try {
            // 生成MinIO预签名URL（有效期60*60=3600秒，即1小时），允许临时访问私有文件
            String url = minioClient.getPresignedObjectUrl(
                    // 构建预签名URL请求参数
                    GetPresignedObjectUrlArgs.builder()
                            // 指定HTTP方法为GET（下载操作）
                            .method(Method.GET)
                            // 指定文件所在的存储桶名称
                            .bucket(bucketName)
                            // 指定要下载的对象名称（即文件名）
                            .object(fileName)
                            // 设置预签名URL的有效期为3600秒（1小时）
                            .expiry(60 * 60)
                            // 构建参数对象
                            .build()
            );
            // 将客户端请求重定向到MinIO预签名URL，浏览器会自动下载文件
            response.sendRedirect(url);
        } catch (Exception e) {
            // 打印异常堆栈信息到控制台
            e.printStackTrace();
            // 设置HTTP响应状态码为500（服务器内部错误）
            response.setStatus(500);
        }
    }
}
