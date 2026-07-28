// 声明该类所属的包路径，com.wll.service包存放自定义业务逻辑服务类（区别于common.service下的通用服务）
package com.wll.service;

// Hutool日期工具类DateUtil，提供日期格式化、日期偏移、获取当前时间字符串等日期相关的静态方法
import cn.hutool.core.date.DateUtil;
// 导入店铺实体类Shop，对应数据库中的店铺表，包含店铺名称、店主ID、店铺类型、状态、拒绝理由等字段
import com.wll.common.entity.Shop;
// 导入用户实体类User，对应数据库中的用户表，包含用户名、密码、角色、联系方式等字段
import com.wll.common.entity.User;
// 导入店铺数据访问接口ShopMapper，MyBatis映射器，提供店铺表的数据库CRUD操作（selectById、insert、updateById、deleteById等）
import com.wll.common.mapper.ShopMapper;
// 导入用户数据访问接口UserMapper，MyBatis映射器，提供用户表的数据库CRUD操作
import com.wll.common.mapper.UserMapper;
// 导入WebSocket事件类型枚举WebSocketEventType，定义推送类型：SHOP_APPLY(店铺申请)、SHOP_APPROVE(审核通过)、SHOP_REJECT(审核拒绝)
import com.wll.common.websocket.WebSocketEventType;
// 导入WebSocket消息体类WebSocketMessage，封装推送消息的数据结构（eventType事件类型、targetUserId目标用户、content消息文本）
import com.wll.common.websocket.WebSocketMessage;
// 导入WebSocket推送服务WebSocketPushService，提供将消息推送到指定用户或Topic（广播）的功能
import com.wll.common.websocket.WebSocketPushService;
// 导入PageHelper分页插件的PageHelper类，调用其startPage方法开启分页拦截（设置页码和每页条数）
import com.github.pagehelper.PageHelper;
// 导入PageHelper分页插件的结果包装类PageInfo，包含分页元信息（数据列表、总条数、总页数、当前页等）
import com.github.pagehelper.PageInfo;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean（MyBatis Mapper等）
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记该类为Spring业务逻辑层组件，被Spring容器自动扫描和管理
import org.springframework.stereotype.Service;

// 导入Java集合框架中的List接口，用于返回查询结果的列表数据
import java.util.List;

// @Service注解：将该类标记为Spring服务层组件，Spring会自动扫描并创建该类的单例Bean
@Service
// 声明ShopService公共类，封装店铺相关的业务逻辑（增删改查、审核流程、WebSocket通知）
public class ShopService {

    // @Resource注解：注入ShopMapper Bean，shopMapper是MyBatis映射器，负责店铺表的SQL操作
    @Resource
    private ShopMapper shopMapper;
    // @Resource注解：注入UserMapper Bean，userMapper是MyBatis映射器，负责用户表的SQL操作（审核通过时更新用户角色）
    @Resource
    private UserMapper userMapper;
    // @Resource注解：注入WebSocketPushService Bean，用于向店主或管理员推送审核状态变更的实时通知
    @Resource
    private WebSocketPushService wsPushService;

    /**
     * 新增店铺
     * 设置店铺的创建时间为当前时间，如果未指定状态则默认为"线上审核中"
     * @param shop 要新增的店铺实体对象，包含店铺名称、店主ID、店铺类型等
     */
    // add方法：新增一个店铺记录到数据库
    public void add(Shop shop) {
        // 设置店铺创建时间为当前时间字符串（格式如：2025-12-05 14:30:00），调用Hutool的DateUtil.now()
        shop.setCreateTime(DateUtil.now());
        // 如果前端未传递店铺状态（status为null），默认设置为"线上审核中"
        if (shop.getStatus() == null) {
            shop.setStatus("线上审核中");
        }
        // 调用MyBatis Mapper将店铺数据插入数据库的shop表
        shopMapper.insert(shop);
    }

    /**
     * 根据主键ID删除店铺
     * @param id 要删除的店铺的主键ID
     */
    // deleteById方法：根据主键ID从数据库删除店铺记录
    public void deleteById(Integer id) {
        // 调用MyBatis Mapper执行DELETE操作
        shopMapper.deleteById(id);
    }

    /**
     * 根据主键ID更新店铺信息
     * @param shop 包含更新后字段值的店铺实体对象，需包含主键ID
     */
    // updateById方法：根据主键ID更新店铺数据库记录
    public void updateById(Shop shop) {
        // 调用MyBatis Mapper执行UPDATE操作，根据shop.getId()作为WHERE条件更新其他字段
        shopMapper.updateById(shop);
    }

    /**
     * 根据主键ID查询单个店铺详情
     * @param id 店铺的主键ID
     * @return Shop 店铺实体对象，不存在时返回null
     */
    // selectById方法：根据主键ID查询店铺数据
    public Shop selectById(Integer id) {
        // 调用MyBatis Mapper执行SELECT操作，返回完整的Shop对象（含所有字段）
        return shopMapper.selectById(id);
    }

    /**
     * 查询所有符合条件的店铺列表
     * @param shop 可选的查询条件对象（字段非空时作为WHERE条件进行等值匹配），传null表示查询全部
     * @return List<Shop> 店铺列表
     */
    // selectAll方法：根据条件查询店铺列表
    public List<Shop> selectAll(Shop shop) {
        // 调用MyBatis Mapper执行动态SQL查询，条件由传入的shop对象中的非空字段决定
        return shopMapper.selectAll(shop);
    }

    /**
     * 分页查询店铺列表
     * @param shop 可选的查询条件对象（非空字段作为WHERE条件）
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页展示的记录条数
     * @return PageInfo<Shop> 包含分页数据（列表、总条数、总页数、页码等）的PageInfo对象
     */
    // selectPage方法：分页查询店铺列表
    public PageInfo<Shop> selectPage(Shop shop, Integer pageNum, Integer pageSize) {
        // 开启PageHelper分页：设置当前页码和每页条数，后续紧跟着的SQL会被自动追加LIMIT子句
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（此时SQL已被PageHelper拦截并添加了物理分页LIMIT）
        List<Shop> list = shopMapper.selectAll(shop);
        // 将查询结果包装为PageInfo对象，PageInfo内自动填充total(总条数)、pages(总页数)、pageNum(当前页)等分页元数据
        return PageInfo.of(list);
    }

    /**
     * 线上审核通过 - 将店铺状态变更为"线下审核中"
     * 线上审核通过后进入线下核查阶段，通过WebSocket通知店主
     * @param id 要审核的店铺的主键ID
     */
    // onlineApprove方法：管理员线上审核通过店铺申请
    public void onlineApprove(Integer id) {
        // 查询店铺是否存在
        Shop shop = shopMapper.selectById(id);
        if (shop != null) {
            // 将店铺状态从"线上审核中"更新为"线下审核中"（进入现场核查阶段）
            shop.setStatus("线下审核中");
            // 更新数据库
            shopMapper.updateById(shop);
            // 如果店主ID不为空，通过WebSocket向店主推送审核进度通知
            if (shop.getOwnerId() != null) {
                wsPushService.pushToUser(shop.getOwnerId(),
                    // 构建WebSocket消息：事件类型=SHOP_APPROVE(审核通过)，目标用户=店主，消息内容包含店铺名
                    new WebSocketMessage(WebSocketEventType.SHOP_APPROVE, shop.getOwnerId(),
                        "您的店铺「" + shop.getName() + "」线上审核已通过，请等待线下核查"));
            }
        }
    }

    /**
     * 线下审核通过 - 将店铺状态变更为"营业中"，同时将用户角色升级为"商家"
     * 线下核查通过后店铺正式开业，通过WebSocket通知店主
     * @param id 要审核的店铺的主键ID
     */
    // offlineApprove方法：线下审核通过，店铺正式营业
    public void offlineApprove(Integer id) {
        // 查询店铺是否存在
        Shop shop = shopMapper.selectById(id);
        if (shop != null) {
            // 将店铺状态更新为"营业中"（正式上线，前端可搜索到该店铺和商品）
            shop.setStatus("营业中");
            // 更新店铺数据库记录
            shopMapper.updateById(shop);
            // 查询店铺店主信息，准备升级用户角色
            User user = userMapper.selectById(shop.getOwnerId());
            // 如果用户存在且当前角色不是"商家"，则将其角色更新为"商家"（解锁商家功能权限）
            if (user != null && !"商家".equals(user.getRole())) {
                user.setRole("商家");
                // 更新用户角色到数据库
                userMapper.updateById(user);
            }
            // 通过WebSocket向店主推送店铺正式开业的成功通知
            if (shop.getOwnerId() != null) {
                wsPushService.pushToUser(shop.getOwnerId(),
                    new WebSocketMessage(WebSocketEventType.SHOP_APPROVE, shop.getOwnerId(),
                        "您的店铺「" + shop.getName() + "」已通过审核，正式营业！"));
            }
        }
    }

    /**
     * 拒绝审核 - 将店铺状态变更为"审核拒绝"，记录拒绝理由
     * 通过WebSocket通知店主，告知拒绝原因并建议修改后重新提交
     * @param id 要拒绝的店铺的主键ID
     * @param reason 拒绝理由（如"资料不完整"、"图片不符合要求"等），可为null
     */
    // reject方法：管理员拒绝店铺申请，附带修改意见
    public void reject(Integer id, String reason) {
        // 查询店铺是否存在
        Shop shop = shopMapper.selectById(id);
        if (shop != null) {
            // 将店铺状态更新为"审核拒绝"
            shop.setStatus("审核拒绝");
            // 记录管理员填写的拒绝理由到数据库
            shop.setRejectReason(reason);
            // 更新店铺数据库记录
            shopMapper.updateById(shop);
            // 通过WebSocket向店主推送审核拒绝通知
            if (shop.getOwnerId() != null) {
                wsPushService.pushToUser(shop.getOwnerId(),
                    // 构建WebSocket消息：事件类型=SHOP_REJECT(审核拒绝)，目标用户=店主
                    new WebSocketMessage(WebSocketEventType.SHOP_REJECT, shop.getOwnerId(),
                        // 消息内容包含店铺名和拒绝原因，若原因未填写则给出默认建议
                        "您的店铺「" + shop.getName() + "」审核被拒绝：" + (reason != null ? reason : "请完善资料后重新提交")));
            }
        }
    }
}
