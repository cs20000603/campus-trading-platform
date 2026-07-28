// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入评论实体类Comment，对应数据库中的评论表，包含用户ID、商品ID、评论内容、评分等字段
import com.wll.common.entity.Comment;
// 导入评论服务接口CommentService，封装评论的增删改查业务逻辑
import com.wll.common.service.CommentService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回评论列表类型数据
import java.util.List;

/**
 * 评论控制器
 * 处理用户对商品的评论/评价相关的增删改查操作
 * 请求路径前缀：/comment
 **/
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/comment前缀
@RequestMapping("/comment")
// 声明CommentController公共类
public class CommentController {

    // @Resource注解：按名称注入CommentService Bean，commentService负责评论数据的增删改查业务逻辑
    @Resource
    private CommentService commentService;

    /**
     * 新增评论
     * 请求方式：POST /comment/add
     * @param comment 评论实体对象（JSON请求体），包含用户ID、商品ID、评论内容、评分等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/comment/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Comment对象，新增一条商品评论
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为Comment Java对象
    public Result add(@RequestBody Comment comment) {
        // 调用服务层将评论数据插入数据库
        commentService.add(comment);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID删除评论
     * 请求方式：DELETE /comment/delete/{id}
     * @param id 评论记录的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，请求路径为/comment/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据路径变量id删除评论
    // @PathVariable注解：绑定URL中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层根据主键ID从数据库删除评论记录
        commentService.deleteById(id);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 修改评论内容
     * 请求方式：PUT /comment/update
     * @param comment 评论实体对象（JSON请求体），包含要修改的评论内容和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，请求路径为/comment/update
    @PutMapping("/update")
    // updateById方法：根据主键更新评论内容
    public Result updateById(@RequestBody Comment comment) {
        // 调用服务层根据主键ID更新评论数据库记录
        commentService.updateById(comment);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单条评论
     * 请求方式：GET /comment/selectById/{id}
     * @param id 评论记录的主键ID（路径参数）
     * @return Result 包含Comment对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/comment/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单条评论详情
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询单条评论记录
        Comment comment = commentService.selectById(id);
        // 返回包含评论数据的成功响应
        return Result.success(comment);
    }

    /**
     * 查询所有评论（可按商品ID等条件过滤）
     * 请求方式：GET /comment/selectAll
     * @param comment 可选的查询条件对象（通过URL参数传递字段值，如goodsId=1）
     * @return Result 包含Comment列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/comment/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的评论记录（如某商品的所有评论）
    public Result selectAll(Comment comment) {
        // 调用服务层查询所有符合条件的评论记录
        List<Comment> list = commentService.selectAll(comment);
        // 返回评论列表
        return Result.success(list);
    }

    /**
     * 分页查询评论
     * 请求方式：GET /comment/selectPage
     * @param comment 可选的查询条件对象（通过URL参数传递字段值，如goodsId=1按商品筛选）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/comment/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询评论列表
    public Result selectPage(Comment comment,
                             // @RequestParam：从URL查询参数获取当前页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：从URL查询参数获取每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<Comment> page = commentService.selectPage(comment, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

}
