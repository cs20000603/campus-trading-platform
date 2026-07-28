// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入管理员实体类Admin（该文件中实际未使用，可能是预留导入或粘贴遗留）
import com.wll.common.entity.Admin;
// 导入用户实体类User，对应数据库中的用户表，包含用户名、密码、真实姓名、联系方式、余额等字段
import com.wll.common.entity.User;
// 导入用户服务接口UserService，封装用户的增删改查、登录注册、密码重置等业务逻辑
import com.wll.common.service.UserService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 处理普通用户的增删改查操作，支持按姓名模糊搜索
 * 请求路径前缀：/user
 */
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/user前缀
@RequestMapping("/user")
// 声明UserController公共类
public class UserController {

    // @Resource注解：按名称注入UserService Bean，userService负责用户数据的增删改查及业务逻辑
    @Resource
    private UserService userService;


    /**
     * 分页查询用户列表（支持按姓名模糊搜索）
     * 请求方式：GET /user/selectPage?pageNum=1&pageSize=10&name=xxx
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @param name 可选的用户姓名搜索关键词，支持模糊匹配（如name=张，可匹配"张三"、"张小明"）
     * @return Result 包含PageInfo分页数据（用户列表、总条数、总页数等）的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/user/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询用户列表，支持姓名模糊搜索
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,     // 页码，默认1
                             @RequestParam(defaultValue = "10") Integer pageSize,   // 每页条数，默认10
                             @RequestParam(required = false) String name) {         // 姓名搜索词，可选
        // 调用服务层分页查询，name不为null时执行LIKE模糊匹配
        PageInfo<User> pageInfo = userService.selectPage(pageNum, pageSize, name);
        // 返回分页数据
        return Result.success(pageInfo);
    }

    /**
     * 根据ID删除用户
     * 请求方式：DELETE /user/delete/{id}
     * 接口示例：/user/delete/1
     * @param id 用户的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，请求路径为/user/delete/{id}
    @DeleteMapping("/delete/{id}")
    // delete方法：根据主键ID删除用户记录
    // @PathVariable注解：绑定URL中的{id}到Integer id参数
    public Result delete(@PathVariable Integer id) {
        // 调用服务层从数据库删除用户
        userService.deleteById(id);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 新增用户
     * 请求方式：POST /user/add
     * @param user 用户实体对象（JSON请求体），包含用户名、密码、真实姓名、联系方式等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/user/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的User对象，新增一个用户
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为User Java对象
    public Result add(@RequestBody User user) {
        // 调用服务层将用户数据插入数据库（通常包含密码加密等处理）
        userService.add(user);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 更新用户信息
     * 请求方式：PUT /user/update
     * @param user 用户实体对象（JSON请求体），包含要修改的字段及主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，请求路径为/user/update
    @PutMapping("/update")
    // update方法：根据主键ID更新用户信息（如修改手机号、地址等）
    public Result update(@RequestBody User user) {
        // 调用服务层根据主键更新用户数据库记录
        userService.update(user);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单个用户信息
     * 请求方式：GET /user/selectById/{id}
     * @param id 用户的主键ID（路径参数）
     * @return Result 包含User对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/user/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单个用户详情
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询用户信息
        User user = userService.selectById(id);
        // 返回包含用户数据的成功响应
        return Result.success(user);
    }

}
