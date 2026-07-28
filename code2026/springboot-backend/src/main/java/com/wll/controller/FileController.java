// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// Hutool工具类-CollUtil：集合工具类，提供newArrayList等便捷方法快速创建集合
import cn.hutool.core.collection.CollUtil;
// Hutool工具类-Dict：键值对字典类，类似HashMap的简化版，支持链式调用set方法
import cn.hutool.core.lang.Dict;
// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// MinIO Java SDK核心客户端接口，提供与MinIO对象存储服务的所有交互方法
import io.minio.*;
// MinIO Http方法枚举，指定预签名URL的HTTP方法类型（GET/PUT等）
import io.minio.http.Method;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean（此处注入MinioClient）
import jakarta.annotation.Resource;
// 导入Jakarta Servlet的HttpServletResponse，用于控制HTTP响应（重定向、设置状态码等）
import jakarta.servlet.http.HttpServletResponse;
// Spring的@Value注解，用于从配置文件(application.yml)中读取配置属性值
import org.springframework.beans.factory.annotation.Value;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@PostMapping、@GetMapping等
import org.springframework.web.bind.annotation.*;
// 导入MultipartFile接口，Spring提供的文件上传抽象，封装上传文件的元数据和输入流
import org.springframework.web.multipart.MultipartFile;

// 导入Java IO中的InputStream，用于读取文件字节流
import java.io.InputStream;
// 导入HashMap，用于构建键值对返回数据
import java.util.HashMap;
// 导入Map接口
import java.util.Map;
// 导入UUID类，用于生成全局唯一的随机字符串作为文件名，防止文件重名冲突
import java.util.UUID;

/**
 * 文件操作控制器（使用MinIO对象存储）
 * 处理文件上传、下载等功能，文件存储在MinIO中
 * 请求路径前缀：/files
 */
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/files前缀
@RequestMapping("/files")
// 声明FileController公共类
public class FileController {

    // @Resource注解：按名称注入MinioClient Bean，minioClient是与MinIO对象存储服务器通信的SDK客户端
    @Resource
    private MinioClient minioClient;

    // @Value注解：从配置文件读取minio.bucket-name属性值，注入bucketName字段（MinIO存储桶名称）
    @Value("${minio.bucket-name}")
    private String bucketName;

    // @Value注解：从配置文件读取minio.endpoint属性值，注入minioEndpoint字段（MinIO服务访问地址，如http://localhost:9000）
    @Value("${minio.endpoint}")
    private String minioEndpoint;

    /**
     * 通用文件上传接口（上传到MinIO）
     * 请求方式：POST /files/upload
     * 自动检查并创建存储桶（bucket），设置桶为公开读权限，
     * 使用UUID生成唯一文件名防止冲突
     * @param file 上传的文件（MultipartFile表单数据）
     * @return Result 包含文件访问URL的成功响应，或上传失败的错误信息
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/files/upload
    @PostMapping("/upload")
    // upload方法：接收前端上传的MultipartFile文件，存储到MinIO并返回访问URL
    public Result upload(MultipartFile file) {
        try {
            // 检查存储桶(bucket)是否已存在，MinIO中的bucket类似于文件系统中的根目录
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                // 如果桶不存在则创建新桶
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            // 设置桶的访问策略为公开读（JSON格式的IAM策略），允许任何人通过URL直接访问桶中的文件
            String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());

            // 获取原始上传文件名（如：avatar.png）
            String originalFilename = file.getOriginalFilename();
            // 提取文件扩展名（如：.png），如果文件名中有"."则截取最后一个"."之后的部分，否则为空字符串
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            // 生成唯一文件名：UUID随机字符串 + 原始扩展名，彻底避免文件重名覆盖问题
            String fileName = UUID.randomUUID().toString() + extension;

            // 执行文件上传：将MultipartFile的输入流写入MinIO的指定桶和对象
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)              // 目标存储桶名称
                    .object(fileName)                 // 目标对象名（即文件名）
                    .stream(file.getInputStream(), file.getSize(), -1)  // 文件输入流、文件大小、分片大小(-1表示不分片)
                    .contentType(file.getContentType())  // 文件MIME类型（如image/png）
                    .build()
            );

            // 拼接文件公开访问URL：MinIO服务地址 + 桶名 + 文件名
            String url = minioEndpoint + "/" + bucketName + "/" + fileName;
            // 返回包含文件URL的成功响应
            return Result.success(url);
        } catch (Exception e) {
            // 打印异常堆栈到控制台，方便排查问题
            e.printStackTrace();
            // 返回上传失败的错误响应，附带具体异常信息
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 文件下载接口（通过临时签名URL重定向）
     * 请求方式：GET /files/download/{fileName}
     * 生成一个有效期1小时的预签名URL，客户端通过重定向下载文件
     * @param fileName 要下载的文件名（路径参数）
     * @param response HTTP响应对象，用于重定向到签名URL
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/files/download/{fileName}
    @GetMapping("/download/{fileName}")
    // download方法：生成文件的临时签名下载URL并重定向
    // @PathVariable注解：绑定URL中的{fileName}到String fileName参数（文件名）
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        try {
            // 生成预签名下载URL：有效期expiry为3600秒（1小时）
            // 预签名URL携带临时签名参数，无需额外认证即可下载
            String url = minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)               // HTTP GET方式下载
                    .bucket(bucketName)                // 文件所在存储桶
                    .object(fileName)                  // 文件对象名
                    .expiry(60 * 60)                   // 有效期3600秒（1小时）
                    .build()
            );
            // HTTP 302重定向到预签名URL，浏览器自动下载文件
            response.sendRedirect(url);
        } catch (Exception e) {
            // 打印异常堆栈
            e.printStackTrace();
            // 发生异常时设置HTTP状态码为500（服务器内部错误）
            response.setStatus(500);
        }
    }

    /**
     * wang-editor富文本编辑器专用文件上传接口
     * 请求方式：POST /files/wang/upload
     * 返回格式遵循wang-editor编辑器规范：
     * errno=0表示成功，errno=1表示失败
     * data中通过url字段返回文件访问地址
     * @param file 上传的图片文件（MultipartFile表单数据）
     * @return Map 包含errno和data字段的响应（符合wang-editor返回值规范）
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/files/wang/upload
    @PostMapping("/wang/upload")
    // wangEditorUpload方法：富文本编辑器图片上传专用接口，返回格式适配wang-editor规范
    public Map<String, Object> wangEditorUpload(MultipartFile file) {
        // 创建返回结果Map
        Map<String, Object> resMap = new HashMap<>();
        try {
            // 检查并创建存储桶
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
            // 设置桶为公开读权限，使上传的图片可以通过URL直接访问
            String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::" + bucketName + "/*\"]}]}";
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());

            // 提取文件扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            // 生成UUID唯一文件名
            String fileName = UUID.randomUUID().toString() + extension;

            // 上传文件到MinIO
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            // 拼接文件访问URL
            String url = minioEndpoint + "/" + bucketName + "/" + fileName;
            // errno=0 符合wang-editor规范表示上传成功
            resMap.put("errno", 0);
            // data字段：wang-editor要求的格式，使用Hutool的CollUtil和Dict构建嵌套结构：[{url: "文件地址"}]
            resMap.put("data", CollUtil.newArrayList(Dict.create().set("url", url)));
        } catch (Exception e) {
            // 打印异常
            e.printStackTrace();
            // errno=1 符合wang-editor规范表示上传失败
            resMap.put("errno", 1);
            // 附带错误信息
            resMap.put("message", "文件上传失败");
        }
        // 返回Map结果（会被Spring MVC自动序列化为JSON）
        return resMap;
    }

}
