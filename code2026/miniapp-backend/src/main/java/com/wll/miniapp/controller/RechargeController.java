// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.controller;

// 导入统一返回结果DTO类，用于封装API响应数据
import com.wll.common.dto.Result;
// 导入充值记录实体类，映射数据库中的充值表
import com.wll.common.entity.Recharge;
// 导入充值服务接口，提供充值记录的添加和查询操作
import com.wll.common.service.RechargeService;
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
// 定义该控制器的基础请求路径为 /recharge
@RequestMapping("/recharge")
// 充值控制器，处理用户账户余额的充值记录添加和查询请求
public class RechargeController {

    // 通过@Resource注解注入充值服务实例（按名称装配）
    @Resource
    // 充值服务接口引用，用于调用充值相关业务逻辑
    private RechargeService rechargeService;

    // 映射POST请求到 /recharge/add，提交充值申请（充值金额写入记录）
    @PostMapping("/add")
    // @RequestBody将请求体JSON绑定到Recharge实体，HttpServletRequest获取当前用户ID
    public Result add(@RequestBody Recharge recharge, HttpServletRequest request) {
        // 从请求属性中获取JWT拦截器解析出的当前用户ID，设置为充值所属用户
        recharge.setUserId((Integer) request.getAttribute("userId"));
        // 调用充值服务执行添加充值记录操作
        rechargeService.add(recharge);
        // 返回提交成功的空结果
        return Result.success();
    }

    // 映射GET请求到 /recharge/selectPage，分页查询当前用户的充值记录
    @GetMapping("/selectPage")
    // @RequestParam获取页码参数，默认第1页
    public Result selectPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            // @RequestParam获取每页条数参数，默认每页10条
            @RequestParam(defaultValue = "10") Integer pageSize,
            // HttpServletRequest获取当前用户ID
            HttpServletRequest request) {
        // 创建充值记录查询条件对象
        Recharge recharge = new Recharge();
        // 设置查询条件：仅查询当前用户的充值记录
        recharge.setUserId((Integer) request.getAttribute("userId"));
        // 调用充值服务进行分页查询，返回当前用户的充值历史列表
        PageInfo<Recharge> page = rechargeService.selectPage(recharge, pageNum, pageSize);
        // 将分页结果包装为成功结果返回
        return Result.success(page);
    }
}
