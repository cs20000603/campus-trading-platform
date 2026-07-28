// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的DateUtil类，提供日期时间工具方法（如now()获取当前时间字符串）
import cn.hutool.core.date.DateUtil;
// 导入Goods实体类，对应数据库goods（商品）表的ORM映射，包含商品名称、价格、库存、图片、分类等字段
import com.wll.common.entity.Goods;
// 导入CartMapper数据访问接口，用于在删除/下架商品时同步清理购物车中关联的记录
import com.wll.common.mapper.CartMapper;
// 导入GoodsMapper数据访问接口，封装对goods表的所有数据库CRUD操作及搜索建议查询
import com.wll.common.mapper.GoodsMapper;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表、总记录数、总页数等分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件
import org.springframework.stereotype.Service;
// 导入Spring的@Transactional注解，用于声明式事务管理，保证多表操作的原子性
import org.springframework.transaction.annotation.Transactional;

// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;

/**
 * 商品业务处理服务
 * 负责商品的增删改查、搜索关键词联想等核心业务
 * 重要业务规则：
 * - 新增商品时自动初始化浏览量为0、销量为0、记录上架时间
 * - 删除商品时必须同步清理所有用户购物车中该商品的记录（多表操作，使用事务保证一致性）
 * - 商品下架时也必须清理购物车中该商品的记录（已下架的商品不应留在购物车中）
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class GoodsService {

    // @Resource 注解：注入GoodsMapper Bean，用于执行goods表的数据库CRUD操作
    @Resource
    private GoodsMapper goodsMapper;
    // @Resource 注解：注入CartMapper Bean，用于在删除/下架商品时清理购物车关联数据
    @Resource
    CartMapper cartMapper;

    /**
     * 新增商品
     * 自动初始化默认字段：浏览量为0、销量为0、记录当前时间为上架时间
     * @param goods 商品实体对象，由调用方构造并传入，需包含名称、价格、库存、分类ID等必要字段
     */
    public void add(Goods goods) {
        // 第一步：初始化浏览量（views）为0，新商品还没有人浏览过
        goods.setViews(0);
        // 第二步：初始化销量（saleCount）为0，新商品还没有售出记录
        goods.setSaleCount(0);
        // 第三步：获取当前时间字符串并设置为上架时间
        String now = DateUtil.now();
        goods.setTime(now);
        // 第四步：调用Mapper层insert方法将商品插入到goods数据库表中
        goodsMapper.insert(goods);
    }

    /**
     * 删除商品（同时清理所有关联的购物车记录）
     * 使用@Transactional注解保证商品删除和购物车清理这两个操作的原子性：
     * 要么同时成功，要么同时回滚，防止出现商品删了但购物车记录还在的不一致状态
     * @param id 商品ID（主键）
     */
    @Transactional  // 开启事务：此方法内的所有数据库操作在一个事务中执行，任何一步失败则全部回滚
    public void deleteById(Integer id) {
        // 第一步：先删除商品主记录（从goods表删除）
        goodsMapper.deleteById(id);
        // 第二步：同步删除所有用户购物车中该商品的记录（从cart表批量删除goodsId匹配的记录）
        // 这确保商品删除后，用户购物车中不会留下"幽灵商品"
        cartMapper.deleteByGoodsId(id);
    }

    /**
     * 更新商品信息
     * 特殊处理：如果商品状态被更新为"下架"，则自动清空所有用户购物车中该商品的记录
     * 已下架的商品不能继续存在于购物车中
     * @param goods 包含更新字段的商品实体对象，id字段定位要更新的记录
     */
    public void updateById(Goods goods) {
        // 第一步：执行商品信息更新（MyBatis根据goods中非空字段动态生成UPDATE SET语句）
        goodsMapper.updateById(goods);
        // 第二步：检查商品状态是否变为"下架"
        if ("下架".equals(goods.getStatus())) {
            // 如果状态是下架，从所有用户的购物车中批量删除该商品的记录
            cartMapper.deleteByGoodsId(goods.getId());
        }
    }

    /**
     * 根据ID查询单个商品详情
     * @param id 商品ID（主键）
     * @return 商品实体对象，如果ID对应的记录不存在则返回null
     */
    public Goods selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单个商品记录
        return goodsMapper.selectById(id);
    }

    /**
     * 根据条件查询所有商品列表
     * 常用于：按分类ID筛选某分类下的商品、按店铺ID筛选某店铺的商品、按状态筛选在售商品等
     * @param goods 查询条件实体，可设置name（商品名模糊查询）、categoryId、shopId、status等字段；传空对象则查全部
     * @return 商品实体列表，无匹配结果时返回空列表（非null）
     */
    public List<Goods> selectAll(Goods goods) {
        // 调用Mapper层selectAll方法，MyBatis根据goods对象中非空字段动态生成WHERE条件
        return goodsMapper.selectAll(goods);
    }

    /**
     * 分页查询商品列表
     * 使用PageHelper分页插件实现物理分页
     * @param goods 查询条件对象，用于筛选商品
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的商品数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<Goods> selectPage(Goods goods, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 将分页参数存入ThreadLocal，后续第一个SQL查询被拦截并自动添加LIMIT
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（自动分页）
        List<Goods> list = goodsMapper.selectAll(goods);
        // 包装为PageInfo对象，自动计算并填充分页统计信息
        return PageInfo.of(list);
    }

    /**
     * 搜索关键词联想提示（自动补全）
     * 根据用户输入的部分关键词，返回数据库中以该关键词开头的商品名称建议列表
     * 用于实现搜索框的自动补全（autocomplete）功能，提升用户体验
     * 例如用户输入"珍珠" -> 返回["珍珠奶茶", ...]
     * @param keyword 用户输入的部分搜索关键词
     * @return 匹配的商品名称字符串列表，无匹配结果时返回空列表
     */
    public List<String> suggest(String keyword) {
        // 调用Mapper层自定义方法selectSuggestions，执行如 SELECT name FROM goods WHERE name LIKE '%keyword%'
        return goodsMapper.selectSuggestions(keyword);
    }

}
