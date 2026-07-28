// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Carousel实体类，对应数据库carousel表的ORM映射，包含轮播图id、图片地址(img)、关联商品ID(goodsId)等字段
import com.wll.common.entity.Carousel;
// 导入CarouselMapper数据访问接口，封装对carousel表的所有数据库CRUD操作
import com.wll.common.mapper.CarouselMapper;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表(list)、总记录数(total)、总页数(pages)等完整分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件，被IoC容器管理
import org.springframework.stereotype.Service;

// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;

/**
 * 轮播图业务处理服务
 * 负责首页轮播图的增删改查操作
 * 轮播图用于在首页顶部展示推荐商品或促销活动，每个轮播图包含图片地址和可选关联的商品ID
 * @Service 注解将本类注册为Spring容器中的单例Bean，默认bean名称为carouselService
 */
@Service
public class CarouselService {

    // @Resource 注解：按照名称从Spring容器中注入CarouselMapper Bean，用于执行carousel表的数据库CRUD操作
    @Resource
    private CarouselMapper carouselMapper;

    /**
     * 新增轮播图记录
     * @param carousel 轮播图实体对象，由调用方（Controller层）构造并传入，需包含图片地址(img)等必要字段
     */
    public void add(Carousel carousel) {
        // 调用Mapper层insert方法将轮播图实体插入到carousel数据库表中
        carouselMapper.insert(carousel);
    }

    /**
     * 根据ID删除轮播图记录
     * @param id 轮播图ID（主键），对应carousel表中的自增id字段
     */
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从carousel表中删除对应记录
        carouselMapper.deleteById(id);
    }

    /**
     * 更新轮播图信息（部分字段更新）
     * @param carousel 包含更新字段的轮播图实体对象，其中id字段用于定位要更新的记录
     */
    public void updateById(Carousel carousel) {
        // 调用Mapper层updateById方法，按主键ID更新轮播图记录（MyBatis动态生成UPDATE SET语句）
        carouselMapper.updateById(carousel);
    }

    /**
     * 根据ID查询单个轮播图
     * @param id 轮播图ID（主键）
     * @return 轮播图实体对象，如果ID对应的记录不存在则返回null
     */
    public Carousel selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单个轮播图记录
        return carouselMapper.selectById(id);
    }

    /**
     * 根据条件查询所有轮播图列表
     * @param carousel 查询条件实体，支持按字段组合筛选；传null则查询全部轮播图记录
     * @return 轮播图实体列表，无匹配结果时返回空列表（非null）
     */
    public List<Carousel> selectAll(Carousel carousel) {
        // 调用Mapper层selectAll方法，MyBatis根据carousel对象中非空字段动态生成WHERE条件
        return carouselMapper.selectAll(carousel);
    }

    /**
     * 分页查询轮播图列表
     * 使用PageHelper分页插件实现物理分页，仅查询当前页所需数据，避免全量查询的性能开销
     * @param carousel 查询条件对象，用于筛选轮播图
     * @param pageNum 当前页码（从1开始），即要查询第几页的数据
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的轮播图数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     *         - getPageNum(): 当前页码
     */
    public PageInfo<Carousel> selectPage(Carousel carousel, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 将分页参数（页码、每页条数）存入ThreadLocal，
        // 其MyBatis拦截器会在紧随其后的第一个SQL查询中自动追加LIMIT offset, size子句
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（此查询会被PageHelper拦截并自动分页，只返回当前页数据而非全表数据）
        List<Carousel> list = carouselMapper.selectAll(carousel);
        // PageInfo.of 将查询结果列表包装为PageInfo对象，自动填充total、pages等分页统计信息
        return PageInfo.of(list);
    }

}
