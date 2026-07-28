// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入店铺实体类Shop，对应数据库中的店铺表，包含店铺名称、店主ID、店铺类型、状态、审核意见等字段
import com.wll.common.entity.Shop;
// 导入店铺数据访问接口ShopMapper，MyBatis映射器，提供店铺的数据库CRUD操作（如按店主查询、查重类型等）
import com.wll.common.mapper.ShopMapper;
// 导入店铺业务服务ShopService，封装店铺的增删改查及审核流程（线上审核、线下审核、拒绝）等业务逻辑
import com.wll.service.ShopService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回店铺类型列表等数据
import java.util.List;
// 导入Java集合框架中的Map接口，用于接收JSON键值对请求体（如拒绝理由）
import java.util.Map;

/**
 * 店铺控制器
 * 处理店铺的增删改查操作，以及店铺审核（线上审批通过/驳回、下线）功能
 * 请求路径前缀：/shop
 */
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/shop前缀
@RequestMapping("/shop")
// 声明ShopController公共类
public class ShopController {

    // @Resource注解：按名称注入ShopService Bean，shopService负责店铺的增删改查和审核业务逻辑
    @Resource
    private ShopService shopService;
    // @Resource注解：按名称注入ShopMapper Bean，shopMapper是MyBatis映射器，直接操作shop表的数据库CRUD
    @Resource
    private ShopMapper shopMapper;

    /**
     * 新增店铺
     * 请求方式：POST /shop/add
     * @param shop 店铺实体对象（JSON请求体），包含店铺名称、店主ID、店铺类型等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/shop/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Shop对象，新增一个店铺
    // @RequestBody注解：将JSON反序列化为Shop对象
    public Result add(@RequestBody Shop shop) {
        // 调用服务层添加店铺（自动设置创建时间和初始状态）
        shopService.add(shop);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 根据ID删除店铺
     * 请求方式：DELETE /shop/delete/{id}
     * @param id 店铺的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，路径为/shop/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据主键ID删除店铺
    // @PathVariable注解：绑定URL路径中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层从数据库删除店铺
        shopService.deleteById(id);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 修改店铺信息
     * 请求方式：PUT /shop/update
     * @param shop 店铺实体对象（JSON请求体），包含要修改的字段和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/shop/update
    @PutMapping("/update")
    // updateById方法：根据主键更新店铺信息
    public Result updateById(@RequestBody Shop shop) {
        // 调用服务层根据主键ID更新店铺数据库记录
        shopService.updateById(shop);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单个店铺详情
     * 请求方式：GET /shop/selectById/{id}
     * @param id 店铺的主键ID（路径参数）
     * @return Result 包含Shop对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/shop/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单个店铺的详细信息
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询店铺详情
        Shop shop = shopService.selectById(id);
        // 返回店铺数据
        return Result.success(shop);
    }

    /**
     * 查询所有店铺（可按条件过滤）
     * 请求方式：GET /shop/selectAll
     * @param shop 可选的查询条件对象（通过URL参数传递字段值，如status="营业中"）
     * @return Result 包含Shop列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/shop/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的店铺
    public Result selectAll(Shop shop) {
        // 调用服务层查询店铺列表
        List<Shop> list = shopService.selectAll(shop);
        // 返回店铺列表
        return Result.success(list);
    }

    /**
     * 分页查询店铺列表
     * 请求方式：GET /shop/selectPage
     * @param shop 可选的查询条件对象（通过URL参数传递字段值，如店铺状态、店铺类型）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/shop/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询店铺列表
    public Result selectPage(Shop shop,
                             // @RequestParam：当前页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<Shop> page = shopService.selectPage(shop, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

    /**
     * 线上审核通过（onlineApprove -> 进入线下审核阶段）
     * 请求方式：PUT /shop/onlineApprove/{id}
     * 将审核通过的店铺状态从"线上审核中"变更为"线下审核中"
     * 审核通过后会通过WebSocket通知店主
     * @param id 店铺的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/shop/onlineApprove/{id}
    @PutMapping("/onlineApprove/{id}")
    // onlineApprove方法：管理员线上审核通过店铺申请，进入线下核查阶段
    public Result onlineApprove(@PathVariable Integer id) {
        // 调用服务层执行线上审核通过：更新状态为"线下审核中"，并通过WebSocket通知店主
        shopService.onlineApprove(id);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 下线店铺（使店铺暂停营业）
     * 请求方式：PUT /shop/offlineApprove/{id}
     * 将店铺状态变更为下线状态，店铺商品将不再对外展示
     * 线下审核通过后执行此操作，店铺正式营业
     * @param id 店铺的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/shop/offlineApprove/{id}
    @PutMapping("/offlineApprove/{id}")
    // offlineApprove方法：线下审核通过，店铺状态变为"营业中"，同时更新用户角色为"商家"
    public Result offlineApprove(@PathVariable Integer id) {
        // 调用服务层执行线下审核通过：更新状态为"营业中"，更新用户角色为商家，并通过WebSocket通知店主
        shopService.offlineApprove(id);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 审核拒绝店铺申请
     * 请求方式：PUT /shop/reject/{id}
     * 驳回店铺的开店申请，可附带拒绝理由
     * @param id 店铺的主键ID（路径参数）
     * @param params JSON请求体，包含拒绝理由，格式：{"reason": "拒绝原因，如资料不完整"}
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/shop/reject/{id}
    @PutMapping("/reject/{id}")
    // reject方法：管理员拒绝店铺申请，附带修改意见
    public Result reject(@PathVariable Integer id, @RequestBody Map<String, String> params) {
        // 从请求体中提取拒绝理由，如果params为空则reason为null
        String reason = params != null ? params.get("reason") : null;
        // 调用服务层执行拒绝：更新状态为"审核拒绝"，记录拒绝理由，并通过WebSocket通知店主
        shopService.reject(id, reason);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 获取所有店铺类型列表（去重）
     * 请求方式：GET /shop/types
     * 从现有店铺数据中提取所有不重复的店铺类型，供前端筛选或下拉选择使用
     * @return Result 包含店铺类型字符串列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，路径为/shop/types
    @GetMapping("/types")
    // types方法：获取系统中所有不重复的店铺类型（如["餐饮","零售","服务"等]）
    public Result types() {
        // 通过ShopMapper的selectDistinctTypes方法从数据库查询所有去重的店铺类型
        List<String> types = shopMapper.selectDistinctTypes();
        // 返回店铺类型列表，前端用于筛选条件或下拉选择器
        return Result.success(types);
    }
}
