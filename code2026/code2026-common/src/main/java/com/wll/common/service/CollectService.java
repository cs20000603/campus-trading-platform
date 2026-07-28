// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的DateUtil类，提供日期时间工具方法（如now()获取当前时间字符串，format()格式化日期等）
import cn.hutool.core.date.DateUtil;
// 导入Collect实体类，对应数据库collect（收藏）表的ORM映射，包含id、用户ID(userId)、商品ID(goodsId)、收藏时间(time)等字段
import com.wll.common.entity.Collect;
// 导入CollectMapper数据访问接口，封装对collect表的所有数据库CRUD操作
import com.wll.common.mapper.CollectMapper;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表、总记录数、总页数等完整分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件，被IoC容器管理
import org.springframework.stereotype.Service;

// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;

/**
 * 收藏业务处理服务
 * 负责用户收藏记录的增删改查操作
 * 核心逻辑：新增收藏记录时自动记录当前时间作为收藏时间，无需调用方手动设置
 * 收藏功能允许用户将感兴趣的商品标记收藏，便于后续查看
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class CollectService {

    // @Resource 注解：按照名称从Spring容器中注入CollectMapper Bean，用于执行collect表的数据库CRUD操作
    @Resource
    private CollectMapper collectMapper;

    /**
     * 新增收藏记录（收藏商品）
     * 自动设置收藏时间为当前系统时间，无需调用方手动设置
     * @param collect 收藏实体对象，必须包含 userId（哪个用户）和 goodsId（哪个商品）字段
     */
    public void add(Collect collect) {
        // 第一步：自动设置收藏时间为当前时间的字符串表示（格式取决于DateUtil.now()的默认格式）
        collect.setTime(DateUtil.now());
        // 第二步：调用Mapper层insert方法将收藏记录插入到collect数据库表中
        collectMapper.insert(collect);
    }

    /**
     * 根据ID删除收藏记录（取消收藏）
     * @param id 收藏记录ID（主键），对应collect表中的自增id字段
     */
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从collect表中删除对应收藏记录
        collectMapper.deleteById(id);
    }

    /**
     * 更新收藏记录
     * @param collect 包含更新字段的收藏实体对象，id字段定位要更新的记录
     */
    public void updateById(Collect collect) {
        // 调用Mapper层updateById方法，按主键ID更新收藏记录
        collectMapper.updateById(collect);
    }

    /**
     * 根据ID查询单条收藏记录
     * @param id 收藏记录ID（主键）
     * @return 收藏实体对象，如果ID对应的记录不存在则返回null
     */
    public Collect selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单条收藏记录
        return collectMapper.selectById(id);
    }

    /**
     * 根据条件查询所有收藏记录（通常按用户ID筛选某用户的所有收藏商品）
     * @param collect 查询条件实体，可设置userId、goodsId等字段作为筛选条件
     * @return 收藏实体列表，无匹配结果时返回空列表（非null）
     */
    public List<Collect> selectAll(Collect collect) {
        // 调用Mapper层selectAll方法，MyBatis根据collect对象中非空字段动态生成WHERE条件
        return collectMapper.selectAll(collect);
    }

    /**
     * 分页查询收藏记录
     * 使用PageHelper分页插件实现物理分页
     * @param collect 查询条件对象，用于筛选收藏记录（通常设置userId查某用户的收藏）
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的收藏数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<Collect> selectPage(Collect collect, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 将分页参数存入ThreadLocal，后续第一个SQL查询会被拦截并自动分页
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（自动分页，只返回当前页的记录）
        List<Collect> list = collectMapper.selectAll(collect);
        // 包装为PageInfo对象，自动填充分页统计信息
        return PageInfo.of(list);
    }

}
