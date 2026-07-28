// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入收藏实体类Collect，对应数据库中的收藏表，包含用户ID、商品ID或店铺ID等字段
import com.wll.common.entity.Collect;
// 导入收藏服务接口CollectService，封装收藏的增删改查业务逻辑
import com.wll.common.service.CollectService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回收藏列表类型数据
import java.util.List;

/**
 * 收藏控制器
 * 处理用户收藏商品/店铺相关的增删改查操作
 * 请求路径前缀：/collect
 **/
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/collect前缀
@RequestMapping("/collect")
// 声明CollectController公共类
public class CollectController {

    // @Resource注解：按名称注入CollectService Bean，collectService负责收藏数据的增删改查业务逻辑
    @Resource
    private CollectService collectService;

    /**
     * 新增收藏记录
     * 请求方式：POST /collect/add
     * @param collect 收藏实体对象（JSON请求体），包含用户ID、商品ID或店铺ID等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/collect/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Collect对象，将商品/店铺添加到用户收藏列表
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为Collect Java对象
    public Result add(@RequestBody Collect collect) {
        // 调用服务层将收藏记录插入数据库
        collectService.add(collect);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID删除收藏记录（取消收藏）
     * 请求方式：DELETE /collect/delete/{id}
     * @param id 收藏记录的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，请求路径为/collect/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据路径变量id取消收藏
    // @PathVariable注解：绑定URL中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层根据主键ID从数据库删除收藏记录
        collectService.deleteById(id);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 修改收藏记录
     * 请求方式：PUT /collect/update
     * @param collect 收藏实体对象（JSON请求体），包含要更新的字段和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，请求路径为/collect/update
    @PutMapping("/update")
    // updateById方法：根据主键更新收藏记录
    public Result updateById(@RequestBody Collect collect) {
        // 调用服务层根据主键ID更新收藏数据库记录
        collectService.updateById(collect);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单条收藏记录
     * 请求方式：GET /collect/selectById/{id}
     * @param id 收藏记录的主键ID（路径参数）
     * @return Result 包含Collect对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/collect/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单条收藏记录详情
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询单条收藏记录
        Collect collect = collectService.selectById(id);
        // 返回包含收藏数据的成功响应
        return Result.success(collect);
    }

    /**
     * 查询所有收藏记录（可按用户ID或商品ID等条件过滤）
     * 请求方式：GET /collect/selectAll
     * @param collect 可选的查询条件对象（通过URL参数传递字段值，如userId=1）
     * @return Result 包含Collect列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/collect/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的收藏记录
    public Result selectAll(Collect collect) {
        // 调用服务层查询所有符合条件的收藏记录
        List<Collect> list = collectService.selectAll(collect);
        // 返回包含收藏列表的成功响应
        return Result.success(list);
    }

    /**
     * 分页查询收藏记录
     * 请求方式：GET /collect/selectPage
     * @param collect 可选的查询条件对象（通过URL参数传递字段值）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据（数据列表、总条数、总页数等）的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/collect/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询收藏记录
    public Result selectPage(Collect collect,
                             // @RequestParam：从URL查询参数获取当前页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：从URL查询参数获取每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<Collect> page = collectService.selectPage(collect, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

}
