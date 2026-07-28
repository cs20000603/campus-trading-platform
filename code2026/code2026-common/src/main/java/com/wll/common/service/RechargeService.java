// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的DateUtil类，提供日期时间工具方法（如now()获取当前时间字符串）
import cn.hutool.core.date.DateUtil;
// 导入Recharge实体类，对应数据库recharge（充值记录）表的ORM映射，包含充值用户ID、充值金额(money)、充值时间(time)等字段
import com.wll.common.entity.Recharge;
// 导入User实体类，用于在充值时更新用户的账户余额
import com.wll.common.entity.User;
// 导入RechargeMapper数据访问接口，封装对recharge表的所有数据库CRUD操作
import com.wll.common.mapper.RechargeMapper;
// 导入UserMapper数据访问接口，用于在充值时读取和更新用户的账户余额
import com.wll.common.mapper.UserMapper;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表、总记录数、总页数等分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件
import org.springframework.stereotype.Service;
// 导入Spring的@Transactional注解，用于声明式事务管理，保证充值记录和余额更新的原子性
import org.springframework.transaction.annotation.Transactional;

// 导入Java标准库的BigDecimal类，用于精确的货币金额运算（避免浮点数精度损失）
import java.math.BigDecimal;
// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;

/**
 * 充值业务处理服务
 * 负责用户账户充值的增删改查操作
 * 核心业务逻辑：新增充值记录时，不仅要插入充值记录表，还要同步更新用户的账户余额（累加充值金额）
 * 涉及两张表的写操作（recharge充值记录表 + user用户表），使用@Transactional保证数据一致性
 * 注意：删除充值记录不会退还余额（设计如此，防止通过删除充值记录套现）
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class RechargeService {

    // @Resource 注解：注入RechargeMapper Bean，用于执行recharge表的数据库CRUD操作
    @Resource
    private RechargeMapper rechargeMapper;
    // @Resource 注解：注入UserMapper Bean，用于在充值时读取和更新用户的账户余额
    @Resource
    UserMapper userMapper;

    /**
     * 新增充值记录，同时更新用户账户余额
     * 这是充值业务的核心方法，完整流程：
     * 1. 查询用户当前的账户余额（为null则视为0）
     * 2. 累加充值金额到用户余额（新余额 = 原余额 + 充值金额）
     * 3. 更新用户表中的余额字段
     * 4. 记录充值时间并插入充值记录表
     * 使用@Transactional保证步骤2-4的原子性：充值记录插入失败则余额更新也会回滚
     * @param recharge 充值实体对象，必须包含 userId（充值用户ID）和 money（充值金额，BigDecimal类型）字段
     */
    @Transactional  // 开启事务：余额更新和充值记录插入在同一事务中，保证数据一致性
    public void add(Recharge recharge) {
        // 第一步：获取充值用户ID并查询用户信息
        Integer userId = recharge.getUserId();
        User user = userMapper.selectById(userId);
        // 第二步：获取用户当前的账户余额，如果为null则视为0（BigDecimal.ZERO）
        BigDecimal currentAccount = user.getAccount();
        if (currentAccount == null) {
            currentAccount = BigDecimal.ZERO;
        }
        // 第三步：累加充值金额到余额（新余额 = 当前余额 + 本次充值金额）
        user.setAccount(currentAccount.add(recharge.getMoney()));
        // 第四步：更新用户表中的余额字段
        userMapper.updateById(user);
        // 第五步：记录充值时间为当前系统时间
        recharge.setTime(DateUtil.now());
        // 第六步：将充值记录插入到recharge数据库表中（用于留存充值历史，方便对账和查询）
        rechargeMapper.insert(recharge);
    }

    /**
     * 根据ID删除充值记录
     * 注意：此方法仅删除充值记录，不会退还用户余额
     * 设计意图：防止通过恶意删除充值记录来实现"充值后退款"的套现行为
     * @param id 充值记录ID（主键）
     */
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从recharge表中删除对应记录
        rechargeMapper.deleteById(id);
    }

    /**
     * 更新充值记录
     * @param recharge 包含更新字段的充值实体对象，id字段定位记录
     */
    public void updateById(Recharge recharge) {
        // 调用Mapper层updateById方法，按主键ID更新充值记录
        rechargeMapper.updateById(recharge);
    }

    /**
     * 根据ID查询单条充值记录
     * @param id 充值记录ID（主键）
     * @return 充值实体对象，如果记录不存在则返回null
     */
    public Recharge selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单条充值记录
        return rechargeMapper.selectById(id);
    }

    /**
     * 根据条件查询所有充值记录（通常按用户ID筛选某用户的所有充值历史）
     * @param recharge 查询条件实体，可设置userId等字段作为筛选条件
     * @return 充值记录列表，无匹配结果时返回空列表（非null）
     */
    public List<Recharge> selectAll(Recharge recharge) {
        // 调用Mapper层selectAll方法，MyBatis根据recharge对象中非空字段动态生成WHERE条件
        return rechargeMapper.selectAll(recharge);
    }

    /**
     * 分页查询充值记录列表
     * 使用PageHelper分页插件实现物理分页
     * @param recharge 查询条件对象，用于筛选充值记录
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的充值记录数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<Recharge> selectPage(Recharge recharge, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 将分页参数存入ThreadLocal，拦截后第一个SQL自动添加LIMIT分页
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（自动分页）
        List<Recharge> list = rechargeMapper.selectAll(recharge);
        // 包装为PageInfo对象，自动填充分页统计信息
        return PageInfo.of(list);
    }

}
