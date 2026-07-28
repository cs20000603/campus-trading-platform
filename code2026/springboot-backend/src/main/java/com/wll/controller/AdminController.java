// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入管理员实体类Admin，对应数据库中的管理员表，包含id、username、password、role等字段
import com.wll.common.entity.Admin;
// 导入闲置商品实体类IdleGoods，对应数据库中的闲置商品表，包含商品名、价格、状态、卖家ID等字段
import com.wll.common.entity.IdleGoods;
// 导入管理员服务接口AdminService，封装管理员用户的业务逻辑（增删改查）
import com.wll.common.service.AdminService;
// 导入数据初始化服务DataInitService，用于一键生成系统演示数据（用户、商品、订单等）
import com.wll.common.service.DataInitService;
// 导入闲置商品服务接口IdleGoodsService，封装闲置商品的业务逻辑（发布、下架、删除等）
import com.wll.common.service.IdleGoodsService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含数据列表、总条数、总页数、当前页码等分页元信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController（RESTful控制器）、@RequestMapping（请求路径映射）等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于接收和返回列表类型数据
import java.util.List;
// 导入Java集合框架中的Map接口，用于接收和返回键值对类型数据
import java.util.Map;

/**
 * 管理员控制器
 * 处理管理员用户的增删改查操作，以及闲置商品的审核管理（下架、删除）
 * 还提供演示数据的初始化功能
 * 请求路径前缀：/admin
 **/
// @RestController注解：标记该类为RESTful控制器，所有方法返回值自动序列化为JSON并写入HTTP响应体
@RestController
// @RequestMapping注解：将该控制器所有接口的请求路径统一映射到/admin前缀下
@RequestMapping("/admin")
// 声明AdminController公共类，继承Object基类
public class AdminController {

    // @Resource注解：按名称注入AdminService Bean，adminService负责管理员用户的增删改查业务逻辑
    @Resource
    private AdminService adminService;
    // @Resource注解：按名称注入IdleGoodsService Bean，idleGoodsService负责闲置商品的业务逻辑（下架、删除）
    @Resource
    private IdleGoodsService idleGoodsService;
    // @Resource注解：按名称注入DataInitService Bean，dataInitService负责批量初始化系统演示数据
    @Resource
    private DataInitService dataInitService;

    /**
     * 新增管理员
     * 请求方式：POST /admin/add
     * @param admin 管理员实体对象（JSON请求体），包含用户名、密码、角色等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：将HTTP POST请求映射到该方法，请求路径为/admin/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Admin对象，处理新增管理员操作
    // @RequestBody注解：将HTTP请求体中的JSON数据反序列化为Admin Java对象
    public Result add(@RequestBody Admin admin) {
        // 调用服务层adminService的add方法，将管理员数据插入数据库
        adminService.add(admin);
        // 返回统一成功响应Result对象（status=200, message="操作成功"）
        return Result.success();
    }

    /**
     * 根据ID删除管理员
     * 请求方式：DELETE /admin/delete/{id}
     * @param id 管理员的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：将HTTP DELETE请求映射到该方法，请求路径为/admin/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：接收路径变量id，根据主键ID删除管理员
    // @PathVariable注解：将URL路径中的{id}占位符值绑定到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层根据ID删除管理员记录
        adminService.deleteById(id);
        // 返回统一成功响应
        return Result.success();
    }

    /**
     * 修改管理员信息
     * 请求方式：PUT /admin/update
     * @param admin 管理员实体对象（JSON请求体），包含要修改的字段和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：将HTTP PUT请求映射到该方法，请求路径为/admin/update
    @PutMapping("/update")
    // updateById方法：接收JSON请求体中的Admin对象，根据主键更新管理员信息
    public Result updateById(@RequestBody Admin admin) {
        // 调用服务层根据主键ID更新管理员数据库记录
        adminService.updateById(admin);
        // 返回统一成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单个管理员信息
     * 请求方式：GET /admin/selectById/{id}
     * @param id 管理员的主键ID（路径参数）
     * @return Result 包含Admin对象的成功响应
     */
    // @GetMapping注解：将HTTP GET请求映射到该方法，请求路径为/admin/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：接收路径变量id，查询单个管理员数据
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层根据主键ID查询管理员记录，返回Admin对象
        Admin admin = adminService.selectById(id);
        // 返回包含管理员数据的成功响应
        return Result.success(admin);
    }

    /**
     * 查询所有管理员
     * 请求方式：GET /admin/selectAll
     * @param admin 可选的查询条件对象（通过URL参数传递字段值，如/admin/selectAll?role=管理员）
     * @return Result 包含Admin列表的成功响应
     */
    // @GetMapping注解：将HTTP GET请求映射到该方法，请求路径为/admin/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：接收可选查询条件对象（通过URL参数自动绑定到Admin对象字段），查询所有符合条件的管理员
    public Result selectAll(Admin admin) {
        // 调用服务层查询所有符合条件的管理员，返回List<Admin>列表
        List<Admin> list = adminService.selectAll(admin);
        // 返回包含管理员列表的成功响应
        return Result.success(list);
    }

    /**
     * 分页查询管理员列表
     * 请求方式：GET /admin/selectPage
     * @param admin 可选的查询条件对象（通过URL参数传递字段值）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：将HTTP GET请求映射到该方法，请求路径为/admin/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询管理员列表
    public Result selectPage(Admin admin,
                             // @RequestParam注解：从URL查询参数获取pageNum，默认值为1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam注解：从URL查询参数获取pageSize，默认值为10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper自动拦截SQL通过LIMIT/OFFSET实现物理分页，返回PageInfo分页信息对象
        PageInfo<Admin> page = adminService.selectPage(admin, pageNum, pageSize);
        // 返回包含分页数据（列表、总条数、总页数、当前页等）的成功响应
        return Result.success(page);
    }

    /**
     * 管理员查看闲置商品管理列表（分页）
     * 请求方式：GET /admin/idleGoods/selectPage
     * 用于管理员后台管理所有用户的闲置商品，可按状态等条件筛选
     * @param idleGoods 可选的查询条件对象（通过URL参数传递字段值）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：请求路径为/admin/idleGoods/selectPage
    @GetMapping("/idleGoods/selectPage")
    // idleGoodsSelectPage方法：管理员分页查看所有闲置商品
    public Result idleGoodsSelectPage(IdleGoods idleGoods,
                                       // @RequestParam：页码参数，默认第1页
                                       @RequestParam(defaultValue = "1") Integer pageNum,
                                       // @RequestParam：每页条数参数，默认10条
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用闲置商品服务层分页查询所有闲置商品
        PageInfo<IdleGoods> page = idleGoodsService.selectPage(idleGoods, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

    /**
     * 管理员强制下架闲置商品
     * 请求方式：PUT /admin/idleGoods/takeDown/{id}
     * 管理员可以对违规或不当的闲置商品执行强制下架操作
     * @param id 闲置商品的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT请求映射，路径为/admin/idleGoods/takeDown/{id}
    @PutMapping("/idleGoods/takeDown/{id}")
    // idleGoodsTakeDown方法：管理员强制下架指定ID的闲置商品
    public Result idleGoodsTakeDown(@PathVariable Integer id) {
        // 调用闲置商品服务层执行下架操作（更新商品状态为"已下架"）
        idleGoodsService.takeDown(id);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 管理员删除闲置商品
     * 请求方式：DELETE /admin/idleGoods/delete/{id}
     * 管理员可以删除违规的闲置商品
     * @param id 闲置商品的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE请求映射，路径为/admin/idleGoods/delete/{id}
    @DeleteMapping("/idleGoods/delete/{id}")
    // idleGoodsDelete方法：管理员删除指定ID的闲置商品
    public Result idleGoodsDelete(@PathVariable Integer id) {
        // 调用闲置商品服务层根据主键ID从数据库删除该商品记录
        idleGoodsService.deleteById(id);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 初始化演示数据
     * 请求方式：POST /admin/initData
     * 一键初始化系统的演示数据，包括用户、商品、订单、分类等示例数据
     * @return Result 包含初始化结果的Map（各类数据的初始化数量统计）
     */
    // @PostMapping注解：HTTP POST请求映射，路径为/admin/initData
    @PostMapping("/initData")
    // initData方法：一键初始化所有演示数据，无需参数
    public Result initData() {
        // 调用数据初始化服务的initAll方法，批量创建用户、商品、订单、分类等演示数据
        // 返回值Map包含各类数据的初始化数量统计（如：{users: 10, goods: 20, orders: 5}）
        Map<String, Object> result = dataInitService.initAll();
        // 返回包含初始化统计结果的Map的成功响应
        return Result.success(result);
    }

}
