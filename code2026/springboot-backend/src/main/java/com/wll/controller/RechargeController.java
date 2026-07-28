// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入充值记录实体类Recharge，对应数据库中的充值表，包含用户ID、充值金额、充值方式、充值状态等字段
import com.wll.common.entity.Recharge;
// 导入充值服务接口RechargeService，封装充值的增删改查业务逻辑
import com.wll.common.service.RechargeService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回充值记录列表
import java.util.List;

/**
 * 充值控制器
 * 处理用户账户充值相关的增删改查操作，管理充值记录
 * 请求路径前缀：/recharge
 **/
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/recharge前缀
@RequestMapping("/recharge")
// 声明RechargeController公共类
public class RechargeController {

    // @Resource注解：按名称注入RechargeService Bean，rechargeService负责充值记录的管理和充值业务逻辑
    @Resource
    private RechargeService rechargeService;

    /**
     * 新增充值记录（用户充值操作）
     * 请求方式：POST /recharge/add
     * @param recharge 充值实体对象（JSON请求体），包含用户ID、充值金额、充值方式等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/recharge/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Recharge对象，处理用户充值并创建充值记录
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为Recharge Java对象
    public Result add(@RequestBody Recharge recharge) {
        // 调用服务层处理充值：创建充值记录、更新用户账户余额等业务逻辑
        rechargeService.add(recharge);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 根据ID删除充值记录
     * 请求方式：DELETE /recharge/delete/{id}
     * @param id 充值记录的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，路径为/recharge/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据主键ID删除充值记录
    // @PathVariable注解：绑定URL中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层从数据库删除充值记录
        rechargeService.deleteById(id);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 修改充值记录（如修改充值状态：待支付→已完成）
     * 请求方式：PUT /recharge/update
     * @param recharge 充值实体对象（JSON请求体），包含要更新的字段和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/recharge/update
    @PutMapping("/update")
    // updateById方法：根据主键更新充值记录（如修改充值状态为"已完成"）
    public Result updateById(@RequestBody Recharge recharge) {
        // 调用服务层根据主键ID更新充值数据库记录
        rechargeService.updateById(recharge);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单条充值记录
     * 请求方式：GET /recharge/selectById/{id}
     * @param id 充值记录的主键ID（路径参数）
     * @return Result 包含Recharge对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/recharge/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单条充值记录详情
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询单条充值记录
        Recharge recharge = rechargeService.selectById(id);
        // 返回充值记录数据
        return Result.success(recharge);
    }

    /**
     * 查询所有充值记录（可按用户ID等条件过滤）
     * 请求方式：GET /recharge/selectAll
     * @param recharge 可选的查询条件对象（通过URL参数传递字段值，如userId=1）
     * @return Result 包含Recharge列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/recharge/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的充值记录
    public Result selectAll(Recharge recharge) {
        // 调用服务层查询充值记录列表
        List<Recharge> list = rechargeService.selectAll(recharge);
        // 返回充值记录列表
        return Result.success(list);
    }

    /**
     * 分页查询充值记录
     * 请求方式：GET /recharge/selectPage
     * @param recharge 可选的查询条件对象（通过URL参数传递字段值）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/recharge/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询充值记录列表
    public Result selectPage(Recharge recharge,
                             // @RequestParam：当前页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<Recharge> page = rechargeService.selectPage(recharge, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

}
