// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 导入轮播图实体类Carousel，对应数据库中的轮播图表，包含图片URL、标题、跳转链接、排序号等字段
import com.wll.common.entity.Carousel;
// 导入轮播图服务接口CarouselService，封装轮播图的增删改查业务逻辑
import com.wll.common.service.CarouselService;
// 导入PageHelper分页插件的结果包装类PageInfo，包含数据列表、总条数、总页数、当前页码等分页元信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@RequestMapping、@GetMapping、@PostMapping等
import org.springframework.web.bind.annotation.*;

// 导入Java集合框架中的List接口，用于接收和返回列表类型数据
import java.util.List;

/**
 * 轮播图控制器
 * 处理首页轮播图的增删改查操作，用于管理前端展示的轮播图片
 * 请求路径前缀：/carousel
 **/
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// @RequestMapping注解：将控制器所有接口统一映射到/carousel前缀
@RequestMapping("/carousel")
// 声明CarouselController公共类
public class CarouselController {

    // @Resource注解：按名称注入CarouselService Bean，carouselService负责轮播图数据的增删改查
    @Resource
    private CarouselService carouselService;

    /**
     * 新增轮播图
     * 请求方式：POST /carousel/add
     * @param carousel 轮播图实体对象（JSON请求体），包含图片URL、标题、跳转链接、排序等信息
     * @return Result 操作成功响应
     */
    // @PostMapping注解：HTTP POST映射，请求路径为/carousel/add
    @PostMapping("/add")
    // add方法：接收JSON请求体中的Carousel对象，新增一条轮播图记录
    // @RequestBody注解：将HTTP请求体中的JSON反序列化为Carousel对象
    public Result add(@RequestBody Carousel carousel) {
        // 调用服务层将轮播图数据插入数据库
        carouselService.add(carousel);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID删除轮播图
     * 请求方式：DELETE /carousel/delete/{id}
     * @param id 轮播图记录的主键ID（路径参数）
     * @return Result 操作成功响应
     */
    // @DeleteMapping注解：HTTP DELETE映射，请求路径为/carousel/delete/{id}
    @DeleteMapping("/delete/{id}")
    // deleteById方法：根据路径变量id删除轮播图记录
    // @PathVariable注解：绑定URL路径中的{id}到Integer id参数
    public Result deleteById(@PathVariable Integer id) {
        // 调用服务层根据主键ID删除轮播图数据库记录
        carouselService.deleteById(id);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 修改轮播图信息
     * 请求方式：PUT /carousel/update
     * @param carousel 轮播图实体对象（JSON请求体），包含要更新的字段和主键ID
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，请求路径为/carousel/update
    @PutMapping("/update")
    // updateById方法：接收JSON请求体中的Carousel对象，根据主键更新轮播图记录
    public Result updateById(@RequestBody Carousel carousel) {
        // 调用服务层根据主键ID更新轮播图数据库记录
        carouselService.updateById(carousel);
        // 返回操作成功响应
        return Result.success();
    }

    /**
     * 根据ID查询单条轮播图记录
     * 请求方式：GET /carousel/selectById/{id}
     * @param id 轮播图记录的主键ID（路径参数）
     * @return Result 包含Carousel对象的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/carousel/selectById/{id}
    @GetMapping("/selectById/{id}")
    // selectById方法：根据主键ID查询单条轮播图详情
    public Result selectById(@PathVariable Integer id) {
        // 调用服务层查询单条轮播图记录，返回Carousel对象
        Carousel carousel = carouselService.selectById(id);
        // 返回包含轮播图数据的成功响应
        return Result.success(carousel);
    }

    /**
     * 查询所有轮播图（可按条件过滤）
     * 请求方式：GET /carousel/selectAll
     * @param carousel 可选的查询条件对象（通过URL参数传递字段值，如status=1表示启用）
     * @return Result 包含Carousel列表的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/carousel/selectAll
    @GetMapping("/selectAll")
    // selectAll方法：查询所有符合条件的轮播图列表，查询条件通过URL参数自动绑定到Carousel对象
    public Result selectAll(Carousel carousel) {
        // 调用服务层查询所有符合条件的轮播图记录
        List<Carousel> list = carouselService.selectAll(carousel);
        // 返回包含轮播图列表的成功响应
        return Result.success(list);
    }

    /**
     * 分页查询轮播图
     * 请求方式：GET /carousel/selectPage
     * @param carousel 可选的查询条件对象（通过URL参数传递字段值）
     * @param pageNum 当前页码，默认值为1
     * @param pageSize 每页展示数量，默认值为10
     * @return Result 包含PageInfo分页数据（数据列表、总条数、总页数等）的成功响应
     */
    // @GetMapping注解：HTTP GET映射，请求路径为/carousel/selectPage
    @GetMapping("/selectPage")
    // selectPage方法：分页查询轮播图列表
    public Result selectPage(Carousel carousel,
                             // @RequestParam：从URL查询参数获取pageNum，默认为1
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             // @RequestParam：从URL查询参数获取pageSize，默认为10
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        // 调用服务层分页查询，PageHelper自动拦截SQL，在SQL末尾追加LIMIT/OFFSET实现物理分页
        PageInfo<Carousel> page = carouselService.selectPage(carousel, pageNum, pageSize);
        // 返回包含分页数据（列表、总条数、总页数、当前页等）的成功响应
        return Result.success(page);
    }

}
