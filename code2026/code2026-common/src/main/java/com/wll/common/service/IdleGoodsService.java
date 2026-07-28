// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的DateUtil类，提供日期时间工具方法（如now()获取当前时间字符串）
import cn.hutool.core.date.DateUtil;
// 导入IdleGoods实体类，对应数据库idle_goods（闲置商品）表的ORM映射，包含标题、价格、图片、卖家ID、状态等字段
import com.wll.common.entity.IdleGoods;
// 导入Shop实体类，用于查询卖家是否有关联的营业中店铺（闲置商品可关联到店铺）
import com.wll.common.entity.Shop;
// 导入User实体类，用于根据卖家ID查询卖家姓名和头像等信息
import com.wll.common.entity.User;
// 导入IdleGoodsMapper数据访问接口，封装对idle_goods表的所有数据库CRUD操作及浏览量自增方法
import com.wll.common.mapper.IdleGoodsMapper;
// 导入ShopMapper数据访问接口，用于查询卖家关联的店铺信息
import com.wll.common.mapper.ShopMapper;
// 导入UserMapper数据访问接口，用于根据卖家ID查询卖家用户信息
import com.wll.common.mapper.UserMapper;
// 导入WebSocket事件类型枚举，定义不同的WebSocket推送消息类型（如IDLE_NEW新品发布、IDLE_SOLD已售出）
import com.wll.common.websocket.WebSocketEventType;
// 导入WebSocket消息类，封装推送给客户端的消息体（包含事件类型、目标用户ID、消息内容）
import com.wll.common.websocket.WebSocketMessage;
// 导入WebSocket推送服务类，负责向指定用户或主题推送实时消息
import com.wll.common.websocket.WebSocketPushService;
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
 * 闲置商品业务处理服务
 * 负责闲置交易模块中闲置商品的发布、查询、下架、标记售出等核心业务
 * 业务规则：
 * - 发布闲置时自动关联卖家信息（姓名、头像），若卖家有营业中的店铺则自动关联店铺ID
 * - 发布成功后通过WebSocket向闲置广场（/topic/idleSquare）广播实时通知
 * - 浏览商品详情时自动增加浏览次数（每次selectById调用浏览量+1）
 * - 商品售出或下架时自动记录对应时间
 * - 标记售出只能针对"在售"状态的商品
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class IdleGoodsService {

    // @Resource 注解：注入IdleGoodsMapper Bean，用于执行idle_goods表的数据库CRUD操作
    @Resource
    private IdleGoodsMapper idleGoodsMapper;
    // @Resource 注解：注入UserMapper Bean，用于查询卖家用户信息（姓名、头像）
    @Resource
    private UserMapper userMapper;
    // @Resource 注解：注入ShopMapper Bean，用于查询卖家是否有关联的店铺
    @Resource
    private ShopMapper shopMapper;
    // @Resource 注解：注入WebSocketPushService Bean，用于向客户端实时推送消息通知
    @Resource
    private WebSocketPushService wsPushService;

    /**
     * 发布闲置商品
     * 完整流程：初始化默认字段 -> 关联卖家信息（姓名/头像）-> 关联卖家店铺 -> 插入数据库 -> WebSocket广播通知
     * 使用@Transactional保证数据库写入的原子性
     * @param idleGoods 闲置商品实体对象，必须包含卖家ID(sellerId)、标题(title)、价格(price)、图片(images)等字段
     */
    @Transactional  // 开启事务：数据库写入操作在事务中执行，失败则全部回滚
    public void add(IdleGoods idleGoods) {
        // 第一步：初始化默认状态为"在售"（新发布的闲置商品默认在售状态）
        idleGoods.setStatus("在售");
        // 第二步：初始化浏览量为0
        idleGoods.setViews(0);
        // 第三步：记录发布时间为当前系统时间
        idleGoods.setCreateTime(DateUtil.now());
        // 第四步：根据卖家ID查询卖家用户信息，自动填充卖家姓名和头像
        User seller = userMapper.selectById(idleGoods.getSellerId());
        if (seller != null) {
            // 如果查询到卖家，则将卖家的姓名和头像设置到闲置商品中
            idleGoods.setSellerName(seller.getName());
            idleGoods.setSellerAvatar(seller.getAvatar());
        }
        // 第五步：查询卖家是否拥有营业中的店铺，如果有则自动关联店铺ID
        Shop shop = shopMapper.selectByOwnerId(idleGoods.getSellerId());
        if (shop != null && "营业中".equals(shop.getStatus())) {
            // 只有店铺状态为"营业中"时才关联，已关闭的店铺不关联
            idleGoods.setShopId(shop.getId());
        }
        // 第六步：将闲置商品实体插入到idle_goods数据库表中
        idleGoodsMapper.insert(idleGoods);
        // 第七步：通过WebSocket向闲置广场广播实时消息，通知所有在线用户有新商品上架
        // pushToTopic 向订阅 /topic/idleSquare 的所有客户端发送消息
        // WebSocketMessage 封装消息：事件类型为IDLE_NEW（新品发布），内容包含商品标题和价格
        wsPushService.pushToTopic("/topic/idleSquare",
            new WebSocketMessage(WebSocketEventType.IDLE_NEW, null,
                "闲置广场有新宝贝：「" + idleGoods.getTitle() + "」仅售￥" + idleGoods.getPrice()));
    }

    /**
     * 删除闲置商品
     * 使用@Transactional保证事务性
     * @param id 闲置商品ID（主键）
     */
    @Transactional  // 开启事务
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从idle_goods表中删除记录
        idleGoodsMapper.deleteById(id);
    }

    /**
     * 更新闲置商品信息
     * 特殊处理：如果状态变更为"已售出"或"已下架"，自动记录售出/下架时间
     * 使用@Transactional保证事务性
     * @param idleGoods 包含更新字段的闲置商品实体对象
     */
    @Transactional  // 开启事务
    public void updateById(IdleGoods idleGoods) {
        // 第一步：检查状态是否为终态（已售出或已下架），如果是则记录当前时间为售出/下架时间
        if ("已售出".equals(idleGoods.getStatus()) || "已下架".equals(idleGoods.getStatus())) {
            idleGoods.setSoldTime(DateUtil.now());
        }
        // 第二步：执行数据库更新操作
        idleGoodsMapper.updateById(idleGoods);
    }

    /**
     * 根据ID查询闲置商品详情
     * 查询前自动将浏览量+1（模拟用户每次查看详情增加一次浏览）
     * 每次调用此方法都会累加浏览量，不需要调用方额外处理
     * @param id 闲置商品ID（主键）
     * @return 闲置商品实体对象（包含最新的浏览量+1后的结果），如果记录不存在则返回null
     */
    public IdleGoods selectById(Integer id) {
        // 第一步：通过Mapper层incrementViews方法执行SQL：UPDATE idle_goods SET views = views + 1 WHERE id = ?
        // 这确保了浏览量的原子性递增（即使并发访问也不会丢失计数）
        idleGoodsMapper.incrementViews(id);
        // 第二步：查询并返回商品详情（此时views已经是+1后的最新值）
        return idleGoodsMapper.selectById(id);
    }

    /**
     * 根据条件查询所有闲置商品列表
     * @param idleGoods 查询条件实体，可设置status（在售/已售出/已下架）、sellerId等字段筛选
     * @return 闲置商品实体列表，无匹配结果时返回空列表（非null）
     */
    public List<IdleGoods> selectAll(IdleGoods idleGoods) {
        // 调用Mapper层selectAll方法，MyBatis根据idleGoods对象中非空字段动态生成WHERE条件
        return idleGoodsMapper.selectAll(idleGoods);
    }

    /**
     * 分页查询闲置商品列表
     * 使用PageHelper分页插件实现物理分页
     * @param idleGoods 查询条件对象，用于筛选闲置商品
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的闲置商品数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<IdleGoods> selectPage(IdleGoods idleGoods, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 将分页参数存入ThreadLocal，拦截后第一个SQL自动添加LIMIT
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（自动分页）
        List<IdleGoods> list = idleGoodsMapper.selectAll(idleGoods);
        // 包装为PageInfo，自动填充分页元数据
        return PageInfo.of(list);
    }

    /**
     * 标记闲置商品为已售出
     * 只能将"在售"状态的商品标记为已售出，其他状态（已售出、已下架）不允许再次标记
     * 设置状态为"已售出"、记录售出时间，并通过WebSocket通知卖家
     * 使用@Transactional保证事务性
     * @param id 闲置商品ID（主键）
     */
    @Transactional  // 开启事务
    public void markAsSold(Integer id) {
        // 第一步：查询商品信息
        IdleGoods goods = idleGoodsMapper.selectById(id);
        // 第二步：校验商品存在且当前状态为"在售"（只有"在售"才能标记为"已售出"）
        if (goods != null && "在售".equals(goods.getStatus())) {
            // 第三步：更新状态为"已售出"
            goods.setStatus("已售出");
            // 第四步：记录售出时间为当前系统时间
            goods.setSoldTime(DateUtil.now());
            // 第五步：将更新后的状态和时间持久化到数据库
            idleGoodsMapper.updateById(goods);
            // 第六步：通过WebSocket向卖家推送消息，通知商品已被拍下
            // pushToUser 向指定用户ID的WebSocket连接发送消息
            wsPushService.pushToUser(goods.getSellerId(),
                new WebSocketMessage(WebSocketEventType.IDLE_SOLD, goods.getSellerId(),
                    "您的闲置「" + goods.getTitle() + "」已被拍下！"));
        }
    }

    /**
     * 下架闲置商品
     * 将商品状态设置为"已下架"并记录下架时间
     * 使用@Transactional保证事务性
     * @param id 闲置商品ID（主键）
     */
    @Transactional  // 开启事务
    public void takeDown(Integer id) {
        // 第一步：查询商品信息
        IdleGoods goods = idleGoodsMapper.selectById(id);
        if (goods != null) {
            // 第二步：设置状态为"已下架"
            goods.setStatus("已下架");
            // 第三步：记录下架时间（复用soldTime字段存储下架时间）
            goods.setSoldTime(DateUtil.now());
            // 第四步：将更新持久化到数据库
            idleGoodsMapper.updateById(goods);
        }
    }
}
