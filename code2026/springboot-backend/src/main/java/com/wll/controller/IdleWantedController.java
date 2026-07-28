// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入闲置求购实体类IdleWanted，对应数据库中的求购表，包含求购物品名称、描述、预算、发布者ID等字段
import com.wll.common.entity.IdleWanted;
// 导入用户实体类User，用于从Session中获取当前登录用户信息
import com.wll.common.entity.User;
// 导入闲置求购服务接口IdleWantedService，封装求购信息的发布、查询、删除等业务逻辑
import com.wll.common.service.IdleWantedService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入HttpSession，用于获取服务器端会话中的登录用户信息
import jakarta.servlet.http.HttpSession;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@PostMapping、@GetMapping、@DeleteMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回求购列表
import java.util.List;

/**
 * 闲置求购控制器
 * 处理校园闲置物品求购信息的发布、管理和查询功能
 * 用户可以发布求购需求，其他用户看到后可以联系发布者
 * 请求路径前缀：/idleWanted
 */
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/idleWanted前缀
@RequestMapping("/idleWanted")
// 声明IdleWantedController公共类
public class IdleWantedController {

    // @Resource注解：按名称注入IdleWantedService Bean，负责求购信息的增删改查业务逻辑
    @Resource
    private IdleWantedService idleWantedService;

    /**
     * 发布求购信息
     * 请求方式：POST /idleWanted/add
     * 需要登录，自动将当前用户设为求购发布者
     * @param idleWanted 求购实体对象（JSON请求体），包含求购物品名称、描述、预算等信息
     * @param session HTTP会话，用于获取当前登录用户
     * @return Result 操作成功响应，未登录时返回错误
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/idleWanted/add
    @PostMapping("/add")
    // add方法：发布一条新的求购信息
    public Result add(@RequestBody IdleWanted idleWanted,
                      // HttpSession参数：获取当前登录用户信息
                      HttpSession session) {
        // 从Session中获取当前登录用户对象
        User user = (User) session.getAttribute("user");
        // 未登录则返回错误提示
        if (user == null) return Result.error("请先登录");
        // 自动设置求购发布者ID为当前登录用户的ID，防止伪造发布者身份
        idleWanted.setUserId(user.getId());
        // 调用服务层将求购信息保存到数据库
        idleWantedService.add(idleWanted);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 删除求购信息（仅发布者本人可删除）
     * 请求方式：DELETE /idleWanted/delete/{id}
     * @param id 求购信息的主键ID（路径参数）
     * @param session HTTP会话，用于获取当前登录用户并验证权限
     * @return Result 操作成功或错误响应（未登录/信息不存在/无权操作）
     */
    // @DeleteMapping注解：HTTP DELETE映射，路径为/idleWanted/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：删除指定ID的求购信息，需验证发布者身份
    public Result deleteById(@PathVariable Integer id, HttpSession session) {
        // 验证登录状态
        User user = (User) session.getAttribute("user");
        if (user == null) return Result.error("请先登录");
        // 查询求购信息是否存在
        IdleWanted wanted = idleWantedService.selectById(id);
        if (wanted == null) return Result.error("信息不存在");
        // 验证当前用户是否为该求购信息的发布者本人（防止越权删除）
        if (!wanted.getUserId().equals(user.getId())) return Result.error("无权操作");
        // 执行删除
        idleWantedService.deleteById(id);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 修改求购信息（仅发布者本人可修改）
     * 请求方式：PUT /idleWanted/update
     * @param idleWanted 求购实体对象（JSON请求体），包含要修改的字段和主键ID
     * @param session HTTP会话，用于获取当前登录用户并验证权限
     * @return Result 操作成功或错误响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/idleWanted/update
    @PutMapping("/update")
    // updateById方法：更新求购信息，需验证发布者身份
    public Result updateById(@RequestBody IdleWanted idleWanted, HttpSession session) {
        // 验证登录状态
        User user = (User) session.getAttribute("user");
        if (user == null) return Result.error("请先登录");
        // 查询数据库中原始求购信息
        IdleWanted exist = idleWantedService.selectById(idleWanted.getId());
        if (exist == null) return Result.error("信息不存在");
        // 验证当前用户是否为该求购信息的发布者本人
        if (!exist.getUserId().equals(user.getId())) return Result.error("无权操作");
        // 执行更新
        idleWantedService.updateById(idleWanted);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 查询所有求购信息（无需登录，公开接口）
     * 请求方式：GET /idleWanted/selectAll
     * @param idleWanted 可选的查询条件对象（通过URL参数传递字段值）
     * @return Result 包含IdleWanted列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/idleWanted/selectAll，公开接口
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的求购信息
    public Result selectAll(IdleWanted idleWanted) {
        // 调用服务层查询求购信息列表
        List<IdleWanted> list = idleWantedService.selectAll(idleWanted);
        // 返回求购信息列表
        return Result.success(list);
    }

    /**
     * 分页查询求购信息（无需登录）
     * 请求方式：GET /idleWanted/selectPage
     * @param idleWanted 可选的查询条件对象（通过URL参数传递字段值）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/idleWanted/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询求购信息列表
    public Result selectPage(IdleWanted idleWanted,
                             // @RequestParam：当前页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<IdleWanted> page = idleWantedService.selectPage(idleWanted, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

    /**
     * 查看我的求购发布记录（需要登录）
     * 请求方式：GET /idleWanted/myWanted?pageNum=1&pageSize=10
     * 只返回当前登录用户发布的求购信息
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @param session HTTP会话，用于获取当前登录用户
     * @return Result 包含当前用户发布的求购信息分页数据
     */
    // @GetMapping注解：HTTP GET映射，路径为/idleWanted/myWanted，需要登录
    @GetMapping("/myWanted")
    // myWanted方法：查询当前登录用户发布的所有求购信息
    public Result myWanted(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           HttpSession session) {
        // 验证登录状态
        User user = (User) session.getAttribute("user");
        if (user == null) return Result.error("请先登录");
        // 构建查询条件：新建IdleWanted对象，设置userId为当前用户ID
        IdleWanted query = new IdleWanted();
        query.setUserId(user.getId());
        // 分页查询，仅返回当前用户的求购信息
        PageInfo<IdleWanted> page = idleWantedService.selectPage(query, pageNum, pageSize);
        // 返回查询结果
        return Result.success(page);
    }
}
