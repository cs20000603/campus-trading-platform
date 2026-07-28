// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入商品分类实体类Category，对应数据库中的分类表，包含分类名称、店铺类型等字段
import com.wll.common.entity.Category;
// 导入分类服务接口CategoryService，封装商品分类的增删改查及按店铺类型筛选业务逻辑
import com.wll.common.service.CategoryService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含数据列表、总条数、总页数等分页元信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回分类列表类型数据
import java.util.List;

/**
 * 商品分类控制器
 * 处理商品分类的增删改查操作，支持按店铺类型筛选分类
 * 请求路径前缀：/category
 **/
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/category前缀
@RequestMapping("/category")
// 声明CategoryController公共类
public class CategoryController {

    // @Resource注解：按名称注入CategoryService Bean，categoryService负责商品分类的增删改查业务逻辑
    @Resource
    private CategoryService categoryService;

    /**
     * 新增商品分类
     * 请求方式：POST /category/add
     * @param category 分类实体对象（JSON请求体），包含分类名称、店铺类型等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/category/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Category对象，新增一个商品分类
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为Category Java对象
    public Result add(@RequestBody Category category) {
        // 调用服务层将分类数据插入数据库
        categoryService.add(category);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID删除商品分类
     * 请求方式：DELETE /category/delete/{id}
     * @param id 分类记录的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，请求路径为/category/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据路径变量id删除分类记录
    // @PathVariable注解：绑定URL中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层根据主键ID从数据库删除分类记录
        categoryService.deleteById(id);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 修改商品分类信息
     * 请求方式：PUT /category/update
     * @param category 分类实体对象（JSON请求体），包含要修改的字段和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，请求路径为/category/update
    @PutMapping("/update")
    // updateById方法：根据主键更新分类信息
    public Result updateById(@RequestBody Category category) {
        // 调用服务层根据主键ID更新分类数据库记录
        categoryService.updateById(category);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单个分类信息
     * 请求方式：GET /category/selectById/{id}
     * @param id 分类记录的主键ID（路径参数）
     * @return Result 包含Category对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/category/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单个分类详情
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询单个分类记录
        Category category = categoryService.selectById(id);
        // 返回包含分类数据的成功响应
        return Result.success(category);
    }

    /**
     * 查询所有分类（可按条件过滤）
     * 请求方式：GET /category/selectAll
     * @param category 可选的查询条件对象（通过URL参数传递字段值，如shopType=餐饮）
     * @return Result 包含Category列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/category/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的分类列表
    public Result selectAll(Category category) {
        // 调用服务层查询所有符合条件的分类记录
        List<Category> list = categoryService.selectAll(category);
        // 返回包含分类列表的成功响应
        return Result.success(list);
    }

    /**
     * 根据店铺类型查询对应分类
     * 请求方式：GET /category/selectByShopType?shopType=xxx
     * 用于不同店铺类型（如餐饮、零售等）展示各自的商品分类
     * @param shopType 店铺类型（可选参数），不传则查询全部分类
     * @return Result 包含该店铺类型下的Category列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/category/selectByShopType
    @GetMapping("/selectByShopType")
    // selectByShopType方法：按店铺类型筛选分类
    // @RequestParam(required = false)：shopType为非必填参数，不传时为null，表示查询全部
    public Result selectByShopType(@RequestParam(required = false) String shopType) {
        // 调用服务层按店铺类型筛选分类列表
        List<Category> list = categoryService.selectByShopType(shopType);
        // 返回分类列表
        return Result.success(list);
    }

    /**
     * 分页查询分类列表
     * 请求方式：GET /category/selectPage
     * @param category 可选的查询条件对象（通过URL参数传递字段值）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/category/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询分类列表
    public Result selectPage(Category category,
                             // @RequestParam：从URL查询参数获取当前页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：从URL查询参数获取每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<Category> page = categoryService.selectPage(category, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

}
