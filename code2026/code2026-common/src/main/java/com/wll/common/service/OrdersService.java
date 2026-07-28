// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的DateUtil类，提供日期时间工具方法（如now()获取当前时间字符串，format()格式化日期）
import cn.hutool.core.date.DateUtil;
// 导入Hutool工具库的RandomUtil类，提供随机数生成工具方法（如randomNumbers生成指定长度的随机数字字符串）
import cn.hutool.core.util.RandomUtil;
// 导入com.wll.common.entity包下的所有实体类（Orders订单、Goods商品、User用户、Cart购物车、Shop店铺、OrderDetail订单详情、IdleGoods闲置商品等）
import com.wll.common.entity.*;
// 导入自定义业务异常类CustomException，用于在业务校验失败时向调用方抛出异常触发Spring事务回滚
import com.wll.common.exception.CustomException;
// 导入com.wll.common.mapper包下的所有Mapper接口（OrdersMapper、GoodsMapper、UserMapper等）
import com.wll.common.mapper.*;
// 导入WebSocket事件类型枚举，定义ORDER_NEW（新订单）、ORDER_STATUS（订单状态变更）等事件类型
import com.wll.common.websocket.WebSocketEventType;
// 导入WebSocket消息类，封装推送给客户端（买卖双方）的消息体
import com.wll.common.websocket.WebSocketMessage;
// 导入WebSocket推送服务类，负责向指定用户实时推送消息
import com.wll.common.websocket.WebSocketPushService;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表、总记录数、总页数等分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件
import org.springframework.stereotype.Service;
// 导入Spring的@Transactional注解，用于声明式事务管理，保证多表联动操作的原子性
import org.springframework.transaction.annotation.Transactional;

// 导入Java标准库的BigDecimal类，用于精确的货币金额运算（避免float/double的浮点数精度损失）
import java.math.BigDecimal;
// 导入Java工具包中的ArrayList和Date等类
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 订单业务处理服务（系统核心服务）
 * 负责普通商品下单、闲置商品下单、订单状态流转、订单查询等核心交易业务
 * 涉及多表联动操作（orders/order_detail/goods/user/cart/shop），使用@Transactional保证事务一致性
 * 业务规则：
 * - 下单流程：生成订单号 -> 校验库存 -> 扣减库存增加销量 -> 创建订单详情 -> 扣减用户余额 -> 清理购物车 -> 通知商家
 * - 订单状态流转：待接单 -> 已出货 -> 已配送 -> 已完成 或 待接单 -> 已取消
 * - 取消订单时：退还用户余额，恢复商品库存和销量
 * - 订单状态变更时：通过WebSocket实时通知买家和商家
 * - 订单编号规则：日期(yyyyMMdd) + 系统时间戳 + 4位随机数字，保证唯一性
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class OrdersService {

    // @Resource 注解：注入OrdersMapper Bean，用于执行orders表的数据库CRUD操作
    @Resource
    private OrdersMapper ordersMapper;
    // @Resource 注解：注入GoodsMapper Bean，用于下单时查询商品信息、扣减库存、更新销量
    @Resource
    GoodsMapper goodsMapper;
    // @Resource 注解：注入UserMapper Bean，用于下单时查询用户信息、扣减余额
    @Resource
    UserMapper userMapper;
    // @Resource 注解：注入OrderDetailMapper Bean，用于创建和查询订单详情记录
    @Resource
    OrderDetailMapper orderDetailMapper;
    // @Resource 注解：注入CartMapper Bean，用于下单后清理购物车记录
    @Resource
    CartMapper cartMapper;
    // @Resource 注解：注入ShopMapper Bean，用于查询店铺及店主信息（推送通知和关联订单）
    @Resource
    ShopMapper shopMapper;
    // @Resource 注解：注入WebSocketPushService Bean，用于向买卖双方实时推送订单相关消息
    @Resource
    WebSocketPushService wsPushService;

    /**
     * 购物车批量下单和单个商品购买通用的下单接口
     * 这是系统中最复杂的业务方法之一，完整的下单流程如下：
     * 1. 设置订单初始状态为"待接单"，记录下单时间
     * 2. 生成唯一订单编号（日期+时间戳+4位随机数）
     * 3. 插入订单主记录获取订单ID
     * 4. 遍历购物车列表中的每个商品：
     *    a. 查询商品信息，识别商品所属店铺
     *    b. 检查库存是否充足（不足则抛异常触发事务回滚）
     *    c. 扣减商品库存，增加商品销量
     *    d. 创建订单详情记录（商品名、价格、数量、图片）
     *    e. 删除对应的购物车记录
     *    f. 累加订单总额
     * 5. 检查用户余额是否足够支付（不足则抛异常触发事务回滚）
     * 6. 从用户账户扣减订单总额
     * 7. 更新订单总额
     * 8. 通过WebSocket通知商家有新订单
     * 使用@Transactional保证所有操作原子性：任一步骤失败则全部回滚
     * @param orders 订单实体对象，必须包含 userId（下单用户ID）和 cartList（购物车商品列表）
     * @throws CustomException 用户不存在时抛出"用户不存在"异常
     * @throws CustomException 库存不足时抛出"XX商品库存不足"异常
     * @throws CustomException 余额不足时抛出"对不起，您的账户余额不足，请充值！"异常
     */
    @Transactional  // 开启事务：此方法内所有数据库操作在同一事务中执行，任何一步失败则全部回滚
    public void add(Orders orders) {
        // 第一步：设置订单初始状态为"待接单"（等待商家确认）
        orders.setStatus("待接单");
        // 第二步：记录下单时间为当前系统时间
        orders.setTime(DateUtil.now());
        // 第三步：生成唯一的订单编号
        // 格式：年月日（如20250901） + 当前毫秒时间戳 + 4位随机数字
        // 这种组合方式既能按日期排序，又能确保唯一性（同一毫秒内+随机数也很难碰撞）
        String orderNo = DateUtil.format(new Date(), "yyyyMMdd") + System.currentTimeMillis() + RandomUtil.randomNumbers(4);
        orders.setOrderNo(orderNo);
        // 第四步：插入订单主记录，MyBatis会回填自增主键到orders对象的id字段
        ordersMapper.insert(orders);
        Integer orderId = orders.getId();  // 获取回填的订单ID，后续创建订单详情时使用
        List<Cart> cartList = orders.getCartList();  // 获取购物车商品列表
        // 第五步：验证下单用户是否存在
        User user = userMapper.selectById(orders.getUserId());
        if (user == null) {
            // 用户不存在，抛出业务异常，Spring事务管理器会将之前已执行的操作全部回滚
            throw new CustomException("用户不存在");
        }
        BigDecimal totalPrice = BigDecimal.ZERO;  // 初始化订单总额为0（使用BigDecimal保证金额精度）
        Integer shopId = null;  // 记录订单关联的店铺ID（取第一个商品的店铺）
        // 第六步：遍历购物车列表，逐个处理每个商品
        for (Cart cart : cartList) {
            Integer goodsId = cart.getGoodsId();  // 当前购物车记录对应的商品ID
            Goods goods = goodsMapper.selectById(goodsId);  // 查询商品信息
            // 识别商品所属店铺（取第一个有店铺的商品所属店铺ID，整个订单关联一个店铺）
            if (shopId == null && goods.getShopId() != null) {
                shopId = goods.getShopId();
                orders.setShopId(shopId);  // 将店铺ID设置到订单中
            }
            // 检查库存：当前库存 < 购买数量，说明库存不足
            if (goods.getStore() < cart.getNum()) {
                // 抛出异常，提示具体哪个商品库存不足，事务回滚
                throw new CustomException(goods.getName() + "商品库存不足");
            }
            // 扣减库存：原库存 - 购买数量
            goods.setStore(goods.getStore() - cart.getNum());
            // 增加销量：原销量 + 购买数量
            goods.setSaleCount(goods.getSaleCount() + cart.getNum());
            // 更新商品表（同时更新库存和销量）
            goodsMapper.updateById(goods);
            // 创建订单详情记录，记录本订单中这一个商品的购买信息
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setNum(cart.getNum());          // 购买数量
            orderDetail.setGoodsId(goodsId);            // 商品ID
            orderDetail.setGoodsImg(goods.getImg());    // 商品图片（冗余存储，防止商品图片后被修改）
            orderDetail.setGoodsName(goods.getName());  // 商品名称（冗余存储，记录下单时的商品名）
            orderDetail.setGoodsPrice(goods.getPrice());// 商品单价（冗余存储，记录下单时的单价）
            orderDetail.setOrderId(orderId);            // 订单ID（关联到主订单）
            // 插入订单详情记录
            orderDetailMapper.insert(orderDetail);

            // 删除下单商品对应的购物车记录（如果是从购物车下单的，cart.getId()不为null）
            if (cart.getId() != null) {
                cartMapper.deleteById(cart.getId());
            }

            // 累加订单总额：单价 × 数量
            // BigDecimal.multiply 和 BigDecimal.add 保证金额计算不会出现浮点数精度问题
            totalPrice = totalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(cart.getNum())));
        }
        // 第七步：检查用户余额是否足够支付订单总额
        // compareTo：余额 < 总额返回负数，余额 == 总额返回0，余额 > 总额返回正数
        if (user.getAccount().compareTo(totalPrice) < 0) {
            // 余额不足，抛出异常，事务回滚（之前扣减的库存也会恢复）
            throw new CustomException("对不起，您的账户余额不足，请充值！");
        }
        // 第八步：扣减用户余额：原余额 - 订单总额
        user.setAccount(user.getAccount().subtract(totalPrice));
        // 更新用户表余额字段
        userMapper.updateById(user);
        // 第九步：更新订单总额到订单主记录
        orders.setTotal(totalPrice);
        ordersMapper.updateById(orders);

        // 第十步：通过WebSocket推送消息通知商家有新订单
        Orders saved = ordersMapper.selectById(orderId);  // 重新查询以获取完整的订单信息
        if (saved != null && saved.getShopId() != null) {
            // 查询订单关联的店铺
            Shop shop = shopMapper.selectById(saved.getShopId());
            if (shop != null && shop.getOwnerId() != null) {
                // 向店铺所有者（商家）推送实时消息通知
                wsPushService.pushToUser(shop.getOwnerId(),
                    new WebSocketMessage(WebSocketEventType.ORDER_NEW, shop.getOwnerId(),
                        "您有新的订单：" + saved.getOrderNo()));
            }
        }
    }

    /**
     * 删除订单（同时删除关联的订单详情记录）
     * 使用@Transactional保证订单和详情记录删除的原子性
     * @param id 订单ID（主键）
     */
    @Transactional  // 开启事务：删除订单和订单详情在同一事务中执行
    public void deleteById(Integer id) {
        // 第一步：先删除订单主记录（从orders表删除）
        ordersMapper.deleteById(id);
        // 第二步：同步删除订单详情记录（从order_detail表批量删除orderId匹配的记录）
        orderDetailMapper.deleteByOrderId(id);
    }

    /**
     * 更新订单状态（订单状态机的核心方法）
     * 处理多种订单状态流转场景：
     * 1. 正常流转：待接单 -> 已出货 -> 已配送 -> 已完成（商家逐级推进）
     * 2. 取消流转：待接单 -> 已取消（商家或用户取消）
     *    取消时需做回退操作：
     *    - 退还用户余额（已支付的金额退回）
     *    - 恢复商品库存（加回之前扣减的库存）
     *    - 恢复商品销量（减去之前增加的销量）
     * 状态变更完成后通过WebSocket实时通知买卖双方
     * 使用@Transactional保证状态更新和回退操作的原子性
     * @param orders 包含新状态的订单实体对象，id字段定位订单，status字段指定新状态
     * @throws CustomException 取消订单时，如果用户不存在则抛出异常
     */
    @Transactional  // 开启事务：状态更新和回退操作必须在同一事务中
    public void updateById(Orders orders) {
        // === 取消订单的特殊处理 ===
        if ("已取消".equals(orders.getStatus())) {
            Integer userId = orders.getUserId();  // 获取下单用户ID
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new CustomException("用户不存在");
            }
            // 退还用户余额：当前余额 + 订单总额
            BigDecimal account = user.getAccount();
            if (account == null) {
                account = BigDecimal.ZERO;  // 如果余额为null，视为0
            }
            user.setAccount(account.add(orders.getTotal()));  // 余额 + 订单金额
            userMapper.updateById(user);  // 更新用户余额
            // 恢复商品库存和销量：遍历订单详情中的每个商品
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            for (OrderDetail detail : orderDetailList) {
                Integer goodsId = detail.getGoodsId();
                Goods goods = goodsMapper.selectById(goodsId);
                if (goods != null) {
                    // 库存加回：原库存 + 购买数量
                    goods.setStore(goods.getStore() + detail.getNum());
                    // 销量减去：原销量 - 购买数量
                    goods.setSaleCount(goods.getSaleCount() - detail.getNum());
                    goodsMapper.updateById(goods);
                }
            }
        }
        // === 更新订单状态（所有状态都执行，包括取消） ===
        ordersMapper.updateById(orders);

        // === WebSocket推送：通知买家和商家订单状态变更 ===
        Orders updated = ordersMapper.selectById(orders.getId());
        if (updated != null) {
            // 根据订单新状态构造不同的通知消息文案
            String msg = "已取消".equals(orders.getStatus()) ? "您的订单已取消" :
                         "已出货".equals(orders.getStatus()) ? "商家已接单，正在备货中" :
                         "已配送".equals(orders.getStatus()) ? "您的订单正在配送中" :
                         "已完成".equals(orders.getStatus()) ? "订单已完成" :
                         "订单状态已更新为：" + orders.getStatus();
            // 通知买家（下单用户）
            if (updated.getUserId() != null) {
                wsPushService.pushToUser(updated.getUserId(),
                    new WebSocketMessage(WebSocketEventType.ORDER_STATUS, updated.getUserId(), msg));
            }
            // 通知商家（店铺所有者），但避免重复通知既是买家又是商家的情况
            if (updated.getShopId() != null) {
                Shop shop = shopMapper.selectById(updated.getShopId());
                if (shop != null && shop.getOwnerId() != null && !shop.getOwnerId().equals(updated.getUserId())) {
                    // 商家的ownerId与买家不同才通知（避免同一个人收到两条相同的消息）
                    wsPushService.pushToUser(shop.getOwnerId(),
                        new WebSocketMessage(WebSocketEventType.ORDER_STATUS, shop.getOwnerId(), msg));
                }
            }
        }
    }

    /**
     * 根据ID查询单个订单
     * @param id 订单ID（主键）
     * @return 订单实体对象，如果记录不存在则返回null
     */
    public Orders selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单个订单记录
        return ordersMapper.selectById(id);
    }

    /**
     * 根据条件查询所有订单（如按用户ID查某用户的所有订单、按状态查待处理订单等）
     * @param orders 查询条件实体，可设置userId、status、shopId等字段作为筛选条件
     * @return 订单实体列表，无匹配结果时返回空列表（非null）
     */
    public List<Orders> selectAll(Orders orders) {
        // 调用Mapper层selectAll方法，MyBatis根据orders对象中非空字段动态生成WHERE条件
        return ordersMapper.selectAll(orders);
    }

    /**
     * 分页查询订单列表（包含每个订单的订单详情子列表）
     * 查询后会自动为每个订单填充其包含的商品详情列表
     * @param orders 查询条件对象，用于筛选订单
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的订单数据列表，每个订单的orderDetailList字段已填充
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 将分页参数存入ThreadLocal，后续第一个SQL查询自动添加LIMIT
        PageHelper.startPage(pageNum, pageSize);
        // 查询订单列表（自动分页）
        List<Orders> list = ordersMapper.selectAll(orders);
        // 为每个订单填充订单详情列表（N+1查询，每个订单单独查一次详情）
        // 如果订单数量大，可考虑优化为批量查询
        for (Orders o : list) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(o.getId());  // 设置订单ID作为查询条件
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            o.setOrderDetailList(orderDetailList);  // 将详情列表设置到订单对象中
        }
        // 包装为PageInfo对象，自动填充分页统计信息
        return PageInfo.of(list);
    }

    /**
     * 商家查询自己店铺的订单（分页，含订单详情）
     * 与selectPage的区别：固定按shopId筛选，支持可选的status筛选
     * @param shopId 店铺ID，用于筛选属于该店铺的订单
     * @param status 订单状态筛选条件，可选值："待接单"/"已出货"/"已配送"/"已完成"/"已取消"；传null或空字符串则查询全部状态
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象，每个订单的orderDetailList字段已填充
     */
    public PageInfo<Orders> selectPageByShopId(Integer shopId, String status, Integer pageNum, Integer pageSize) {
        // 构造查询条件对象
        Orders query = new Orders();
        query.setShopId(shopId);  // 按店铺ID筛选
        // 如果传了状态筛选条件则设置，否则不设置（查询全部状态）
        if (status != null && !status.isEmpty()) {
            query.setStatus(status);
        }
        // PageHelper分页
        PageHelper.startPage(pageNum, pageSize);
        // 查询订单列表（自动分页）
        List<Orders> list = ordersMapper.selectAll(query);
        // 为每个订单填充订单详情子列表
        for (Orders o : list) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(o.getId());
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(orderDetail);
            o.setOrderDetailList(orderDetailList);
        }
        // 包装为PageInfo
        return PageInfo.of(list);
    }

    /**
     * 闲置商品直接购买下单（单品直接购买，非购物车批量下单）
     * 不同于普通下单方法(add)，此方法用于闲置广场的单品直接购买场景
     * 流程：验证买家存在 -> 检查余额 -> 创建订单主记录 -> 创建订单详情 -> 扣减余额 -> WebSocket通知卖家
     * 使用@Transactional保证事务原子性
     * @param buyerId 买家用户ID
     * @param idleGoods 被购买的闲置商品实体对象，包含标题(title)、价格(price)、图片(images)、卖家ID(sellerId)、店铺ID(shopId)等信息
     * @throws CustomException 买家用户不存在时抛出"用户不存在"异常
     * @throws CustomException 余额不足时抛出"余额不足，请先充值"异常
     */
    @Transactional  // 开启事务：所有数据库操作在同一事务中
    public void addIdleOrder(Integer buyerId, IdleGoods idleGoods) {
        // 第一步：验证买家用户是否存在
        User buyer = userMapper.selectById(buyerId);
        if (buyer == null) {
            throw new CustomException("用户不存在");
        }
        // 第二步：检查买家余额是否足够支付该闲置商品的价格
        if (buyer.getAccount().compareTo(idleGoods.getPrice()) < 0) {
            throw new CustomException("余额不足，请先充值");
        }
        // 第三步：创建订单主记录
        Orders orders = new Orders();
        orders.setUserId(buyerId);                    // 下单用户ID
        orders.setStatus("待接单");                    // 初始状态：待接单
        orders.setTime(DateUtil.now());                // 下单时间
        // 生成订单编号（日期 + 时间戳 + 4位随机数）
        String orderNo = DateUtil.format(new Date(), "yyyyMMdd") + System.currentTimeMillis() + RandomUtil.randomNumbers(4);
        orders.setOrderNo(orderNo);
        orders.setTotal(idleGoods.getPrice());         // 订单总额 = 闲置商品价格
        orders.setShopId(idleGoods.getShopId());       // 关联店铺ID（如果闲置商品有店铺）
        ordersMapper.insert(orders);                   // 插入订单，回填自增ID
        // 第四步：创建订单详情记录（闲置商品只有一件，数量固定为1）
        OrderDetail detail = new OrderDetail();
        detail.setOrderId(orders.getId());             // 关联订单ID
        detail.setGoodsName(idleGoods.getTitle());     // 商品名称（使用闲置商品标题）
        // 处理图片：闲置商品的images字段可能是逗号分隔的多张图片，取第一张作为订单展示图
        String[] imgs = idleGoods.getImages().split(",");
        detail.setGoodsImg(imgs.length > 0 ? imgs[0] : "");  // 取第一张图片，没有则为空字符串
        detail.setGoodsPrice(idleGoods.getPrice());    // 商品单价
        detail.setNum(1);                              // 闲置商品数量固定为1
        detail.setGoodsId(0);                          // 闲置商品没有标准的goods表ID，设为0表示非普通商品
        orderDetailMapper.insert(detail);              // 插入订单详情
        // 第五步：扣减买家余额
        buyer.setAccount(buyer.getAccount().subtract(idleGoods.getPrice()));
        userMapper.updateById(buyer);
        // 第六步：通过WebSocket通知卖家有人购买了TA的闲置商品
        wsPushService.pushToUser(idleGoods.getSellerId(),
            new WebSocketMessage(WebSocketEventType.ORDER_NEW, idleGoods.getSellerId(),
                "有人购买了您的闲置「" + idleGoods.getTitle() + "」，订单号：" + orderNo));
    }

}
