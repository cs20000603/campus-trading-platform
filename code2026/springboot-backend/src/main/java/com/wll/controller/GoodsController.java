// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入商品实体类Goods，对应数据库中的商品表，包含名称、价格、库存、分类ID、图片、描述等字段
import com.wll.common.entity.Goods;
// 导入商品服务接口GoodsService，封装商品的增删改查及搜索建议等业务逻辑
import com.wll.common.service.GoodsService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于返回商品列表类型数据
import java.util.List;

/**
 * 商品控制器
 * 处理商品相关的增删改查操作，以及搜索建议功能
 * 请求路径前缀：/goods
 **/
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/goods前缀
@RequestMapping("/goods")
// 声明GoodsController公共类
public class GoodsController {

    // @Resource注解：按名称注入GoodsService Bean，goodsService负责商品数据的增删改查和搜索建议业务逻辑
    @Resource
    private GoodsService goodsService;

    /**
     * 新增商品
     * 请求方式：POST /goods/add
     * @param goods 商品实体对象（JSON请求体），包含名称、价格、库存、分类、图片等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/goods/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Goods对象，新增一个商品
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为Goods Java对象
    public Result add(@RequestBody Goods goods) {
        // 调用服务层将商品数据插入数据库
        goodsService.add(goods);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID删除商品
     * 请求方式：DELETE /goods/delete/{id}
     * @param id 商品的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，请求路径为/goods/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据路径变量id删除商品
    // @PathVariable注解：绑定URL中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层根据主键ID从数据库删除商品
        goodsService.deleteById(id);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 修改商品信息
     * 请求方式：PUT /goods/update
     * @param goods 商品实体对象（JSON请求体），包含要修改的字段和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，请求路径为/goods/update
    @PutMapping("/update")
    // updateById方法：根据主键更新商品信息
    public Result updateById(@RequestBody Goods goods) {
        // 调用服务层根据主键ID更新商品数据库记录
        goodsService.updateById(goods);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单个商品详情
     * 请求方式：GET /goods/selectById/{id}
     * @param id 商品的主键ID（路径参数）
     * @return Result 包含Goods对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/goods/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单个商品详情（前端商品详情页调用）
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询单个商品记录
        Goods goods = goodsService.selectById(id);
        // 返回包含商品详情数据的成功响应
        return Result.success(goods);
    }

    /**
     * 查询所有商品（可按分类等条件过滤）
     * 请求方式：GET /goods/selectAll
     * @param goods 可选的查询条件对象（通过URL参数传递字段值，如categoryId=1）
     * @return Result 包含Goods列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/goods/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的商品列表
    public Result selectAll(Goods goods) {
        // 调用服务层查询所有符合条件的商品
        List<Goods> list = goodsService.selectAll(goods);
        // 返回商品列表
        return Result.success(list);
    }

    /**
     * 分页查询商品列表
     * 请求方式：GET /goods/selectPage
     * @param goods 可选的查询条件对象（通过URL参数传递字段值，如分类ID、店铺ID）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/goods/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询商品列表
    public Result selectPage(Goods goods,
                             // @RequestParam：从URL查询参数获取当前页码，默认1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：从URL查询参数获取每页条数，默认10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper在SQL末尾追加LIMIT实现物理分页
        PageInfo<Goods> page = goodsService.selectPage(goods, pageNum, pageSize);
        // 返回分页数据
        return Result.success(page);
    }

    /**
     * 搜索建议（输入联想）
     * 请求方式：GET /goods/suggest?keyword=xxx
     * 根据用户输入的关键词，返回匹配的商品名称建议列表，用于搜索框自动补全
     * @param keyword 用户输入的搜索关键词
     * @return Result 包含匹配的商品名称字符串列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/goods/suggest
    @GetMapping("/suggest")
    // suggest方法：根据关键词返回商品名称联想建议列表
    // @RequestParam：从URL查询参数获取keyword（必填），用于模糊匹配商品名称
    public Result suggest(@RequestParam String keyword) {
        // 调用服务层根据关键词查询匹配的商品名称建议列表（如输入"手机"返回["华为手机","苹果手机"等]）
        List<String> list = goodsService.suggest(keyword);
        // 返回包含建议列表的成功响应，前端用于搜索框下拉自动补全
        return Result.success(list);
    }

}
