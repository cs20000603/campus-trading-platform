// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入评论实体类，映射数据库中的评论表
import com.wll.common.entity.Comment;
// 导入评论服务接口，提供评论的增删改查等操作
import com.wll.common.service.CommentService;
// 导入PageHelper分页插件返回的分页信息类
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解，用于依赖注入
import jakarta.annotation.Resource;
// 导入HttpServletRequest，用于获取JWT拦截器设置的userId属性
import jakarta.servlet.http.HttpServletRequest;
// 导入Spring MVC的REST相关注解
import org.springframework.web.bind.annotation.*;

// 标记该类为REST控制器，所有方法返回值自动序列化为JSON格式
@RestController
// 定义该控制器的基础请求路径为 /comment
@RequestMapping("/comment")
// 评论控制器，处理商品评论的添加、修改、删除、分页查询等请求
public class CommentController {

    // 通过@Resource注解注入评论服务实例（按名称装配）
    @Resource
    // 评论服务接口引用，用于调用评论相关业务逻辑
    private CommentService commentService;

    // 映射POST请求到 /comment/add，用户发表新评论（需登录，由JWT拦截器保护）
    @PostMapping("/add")
    // @RequestBody将请求体JSON绑定到Comment实体，HttpServletRequest获取当前用户ID
    public Result add(@RequestBody Comment comment, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID，设置为评论发布者
        comment.setUserId((Integer) request.getAttribute("userId"));
        // 调用评论服务执行添加操作
        commentService.add(comment);
        // 返回添加成功的空结果
        return Result.success();
    }

    // 映射PUT请求到 /comment/update，用户修改自己发表的评论
    @PutMapping("/update")
    // @RequestBody将请求体JSON绑定到Comment实体（包含要更新的ID和新内容）
    public Result updateById(@RequestBody Comment comment) {
        // 调用评论服务根据实体中的ID更新评论内容
        commentService.updateById(comment);
        // 返回更新成功的空结果
        return Result.success();
    }

    // 映射DELETE请求到 /comment/delete/{id}，根据ID删除评论
    @DeleteMapping("/delete/{id}")
    // @PathVariable从URL路径中提取评论记录ID
    public Result deleteById(@PathVariable Integer id) {
        // 调用评论服务根据ID删除指定评论记录
        commentService.deleteById(id);
        // 返回删除成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /comment/selectPage，分页查询评论（无需登录，公开访问）
    @GetMapping("/selectPage")
    // Comment实体作为查询条件接收参数（如goodsId按商品筛选），其他参数通过URL传递
    public Result selectPage(Comment comment,
                             // @RequestParam获取页码参数，默认第1页
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam获取每页条数参数，默认每页10条
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用评论服务进行分页查询，传入查询条件、页码和每页条数
        PageInfo<Comment> page = commentService.selectPage(comment, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }

    // 映射GET请求到 /comment/selectAll，查询全部评论（无需登录，公开访问）
    @GetMapping("/selectAll")
    // Comment实体作为查询条件接收参数（如goodsId按商品筛选）
    public Result selectAll(Comment comment) {
        // 调用评论服务按条件查询全部评论，并直接将结果包装为成功结果返回
        return Result.success(commentService.selectAll(comment));
    }
}
