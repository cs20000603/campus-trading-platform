// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Cart实体类，对应数据库cart（购物车）表的ORM映射，包含id、商品ID(goodsId)、用户ID(userId)、数量(num)等字段
import com.wll.common.entity.Cart;
// 导入CartMapper数据访问接口，封装对cart表的所有数据库CRUD操作及自定义查询（如selectByGoodsIdAndUserId）
import com.wll.common.mapper.CartMapper;
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
 * 购物车业务处理服务
 * 负责购物车的增删改查操作
 * 核心业务逻辑：添加商品到购物车时，若该用户购物车中已存在相同商品，则累加数量而非重复创建新记录
 *               这样可以避免同一用户对同一商品在购物车中出现多条记录
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class CartService {

    // @Resource 注解：按照名称从Spring容器中注入CartMapper Bean，用于执行cart表的数据库CRUD操作
    @Resource
    private CartMapper cartMapper;

    /**
     * 添加商品到购物车
     * 智能添加逻辑：先查询用户购物车中是否已存在相同商品（同一用户+同一商品ID），
     * 如果已存在则累加购买数量（新数量=原数量+新增数量），如果不存在则插入新记录
     * 这种设计避免了同一商品在购物车中出现多条重复记录，用户体验更好
     * @param cart 购物车实体对象，必须包含 goodsId（商品ID）、userId（用户ID）、num（购买数量）字段
     */
    public void add(Cart cart) {
        // 第一步：查询该用户的购物车中是否已存在相同商品
        Cart dbCart = cartMapper.selectByGoodsIdAndUserId(cart.getGoodsId(), cart.getUserId());
        if (dbCart != null) {
            // 第二步A（已有记录）：累加购买数量 —— 原数量 + 新增加的数量
            dbCart.setNum(dbCart.getNum() + cart.getNum());
            // 调用Mapper层updateById更新该条购物车记录的商品数量
            cartMapper.updateById(dbCart);
        } else {
            // 第二步B（无记录）：直接插入一条新的购物车记录
            cartMapper.insert(cart);
        }
    }

    /**
     * 根据ID删除购物车记录（从购物车中移除某个商品）
     * @param id 购物车记录ID（主键），对应cart表中的自增id字段
     */
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从cart表中删除对应购物车记录
        cartMapper.deleteById(id);
    }

    /**
     * 更新购物车记录（如修改购买数量）
     * @param cart 包含更新字段的购物车实体，id字段定位记录，其余非null字段将被更新
     */
    public void updateById(Cart cart) {
        // 调用Mapper层updateById方法，按主键ID更新购物车记录
        cartMapper.updateById(cart);
    }

    /**
     * 根据ID查询单条购物车记录
     * @param id 购物车记录ID（主键）
     * @return 购物车实体对象，如果ID对应的记录不存在则返回null
     */
    public Cart selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单条购物车记录
        return cartMapper.selectById(id);
    }

    /**
     * 根据条件查询所有购物车记录（通常按用户ID筛选某用户的所有购物车商品）
     * @param cart 查询条件实体，可设置userId等字段作为筛选条件；传空对象则查全部记录
     * @return 购物车实体列表，无匹配结果时返回空列表（非null）
     */
    public List<Cart> selectAll(Cart cart) {
        // 调用Mapper层selectAll方法，MyBatis根据cart对象中非空字段动态生成WHERE条件
        return cartMapper.selectAll(cart);
    }

    /**
     * 分页查询购物车记录
     * 使用PageHelper分页插件实现物理分页
     * @param cart 查询条件对象，用于筛选购物车记录（通常设置userId筛选某用户的购物车）
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的购物车数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<Cart> selectPage(Cart cart, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 设置分页参数，后续第一个SQL查询会被拦截并自动添加LIMIT分页
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（自动分页）
        List<Cart> list = cartMapper.selectAll(cart);
        // 将查询结果列表包装为PageInfo对象，自动填充分页统计信息
        return PageInfo.of(list);
    }

}
