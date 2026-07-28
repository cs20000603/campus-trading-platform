// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的DateUtil类，提供日期时间工具方法（如now()获取当前时间字符串）
import cn.hutool.core.date.DateUtil;
// 导入IdleWanted实体类，对应数据库idle_wanted（闲置求购）表的ORM映射，
// 包含求购商品描述、预算价格、发布用户ID、状态等字段
import com.wll.common.entity.IdleWanted;
// 导入User实体类，用于根据发布者ID查询用户姓名和头像
import com.wll.common.entity.User;
// 导入IdleWantedMapper数据访问接口，封装对idle_wanted表的所有数据库CRUD操作
import com.wll.common.mapper.IdleWantedMapper;
// 导入UserMapper数据访问接口，用于查询发布求购信息的用户详情
import com.wll.common.mapper.UserMapper;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表、总记录数、总页数等分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件
import org.springframework.stereotype.Service;
// 导入Spring的@Transactional注解，用于声明式事务管理
import org.springframework.transaction.annotation.Transactional;

// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;

/**
 * 闲置求购业务处理服务
 * 负责闲置交易模块中求购信息的发布、查询、更新、删除等业务
 * 业务规则：
 * - 发布求购时自动设置状态为"求购中"，记录发布时间
 * - 发布求购时自动关联发布者的姓名和头像（冗余存储，减少连表查询）
 * - 使用@Transactional保证写操作的原子性
 * 求购功能允许用户发布"我想买XX"的需求，方便卖家主动联系
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class IdleWantedService {

    // @Resource 注解：注入IdleWantedMapper Bean，用于执行idle_wanted表的数据库CRUD操作
    @Resource
    private IdleWantedMapper idleWantedMapper;
    // @Resource 注解：注入UserMapper Bean，用于根据userId查询发布者用户信息（姓名、头像）
    @Resource
    private UserMapper userMapper;

    /**
     * 发布求购信息
     * 完整流程：初始化状态和时间 -> 关联发布者信息（姓名/头像）-> 插入数据库
     * 使用@Transactional保证数据库写入的原子性
     * @param idleWanted 求购实体对象，必须包含 userId（发布者ID）、title（求购标题/商品名）、price（预算价格）等字段
     */
    @Transactional  // 开启事务：保证insert操作的原子性
    public void add(IdleWanted idleWanted) {
        // 第一步：设置初始状态为"求购中"（新发布的求购默认正在求购）
        idleWanted.setStatus("求购中");
        // 第二步：记录发布时间为当前系统时间
        idleWanted.setCreateTime(DateUtil.now());
        // 第三步：根据发布者ID查询用户信息，自动填充发布者姓名和头像（冗余存储，方便前端直接展示）
        User user = userMapper.selectById(idleWanted.getUserId());
        if (user != null) {
            // 如果查到用户信息，设置姓名和头像到求购实体中
            idleWanted.setUserName(user.getName());
            idleWanted.setUserAvatar(user.getAvatar());
        }
        // 第四步：将求购实体插入到idle_wanted数据库表中
        idleWantedMapper.insert(idleWanted);
    }

    /**
     * 删除求购信息
     * 使用@Transactional保证事务性
     * @param id 求购信息ID（主键）
     */
    @Transactional  // 开启事务
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从idle_wanted表中删除对应记录
        idleWantedMapper.deleteById(id);
    }

    /**
     * 更新求购信息（如修改描述文字、调整预算价格、修改状态等）
     * 使用@Transactional保证事务性
     * @param idleWanted 包含更新字段的求购实体对象，id字段定位要更新的记录
     */
    @Transactional  // 开启事务
    public void updateById(IdleWanted idleWanted) {
        // 调用Mapper层updateById方法，按主键ID更新求购记录
        idleWantedMapper.updateById(idleWanted);
    }

    /**
     * 根据ID查询求购详情
     * @param id 求购信息ID（主键）
     * @return 求购实体对象，如果记录不存在则返回null
     */
    public IdleWanted selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单条求购记录
        return idleWantedMapper.selectById(id);
    }

    /**
     * 根据条件查询所有求购信息（如按发布者ID筛选某用户发布的所有求购）
     * @param idleWanted 查询条件实体，可设置userId、status等字段作为筛选条件
     * @return 求购实体列表，无匹配结果时返回空列表（非null）
     */
    public List<IdleWanted> selectAll(IdleWanted idleWanted) {
        // 调用Mapper层selectAll方法，MyBatis根据idleWanted对象中非空字段动态生成WHERE条件
        return idleWantedMapper.selectAll(idleWanted);
    }

    /**
     * 分页查询求购信息列表
     * 使用PageHelper分页插件实现物理分页
     * @param idleWanted 查询条件对象，用于筛选求购信息
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的求购数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<IdleWanted> selectPage(IdleWanted idleWanted, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 将分页参数存入ThreadLocal，拦截后第一个SQL自动添加LIMIT分页
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（自动分页，只返回当前页的记录）
        List<IdleWanted> list = idleWantedMapper.selectAll(idleWanted);
        // 包装为PageInfo对象，自动计算total、pages等分页元数据
        return PageInfo.of(list);
    }
}
