// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的ObjectUtil类，提供对象判空、判非空等通用工具方法（如isNull、isNotNull、isEmpty）
import cn.hutool.core.util.ObjectUtil;
// 导入Account实体类，用于登录验证和密码修改时承载用户名、密码、新密码等信息
import com.wll.common.entity.Account;
// 导入Admin实体类，对应数据库admin表的ORM映射，包含管理员的所有属性字段
import com.wll.common.entity.Admin;
// 导入自定义业务异常类CustomException，用于在业务校验失败时向调用方抛出可读的错误信息
import com.wll.common.exception.CustomException;
// 导入AdminMapper数据访问接口，封装对admin表的所有数据库CRUD操作（insert/delete/update/select）
import com.wll.common.mapper.AdminMapper;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表、总记录数、总页数、页码等完整分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段（等价于@Autowired + @Qualifier）
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件，被Spring IoC容器管理并支持依赖注入
import org.springframework.stereotype.Service;

// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;

/**
 * 管理员业务处理服务
 * 负责管理员的增删改查、登录验证、密码修改等核心业务逻辑
 * @Service 注解将本类注册为Spring容器中的单例Bean，默认bean名称为类名首字母小写（adminService）
 */
@Service
public class AdminService {

    // @Resource 注解：按照名称从Spring容器中注入AdminMapper Bean，用于执行admin表的数据库操作
    @Resource
    private AdminMapper adminMapper;

    /**
     * 新增管理员
     * 检查用户名是否已存在（避免重复注册），为未设置密码/姓名的管理员设置默认值，角色默认为"管理员"
     * @param admin 管理员实体对象，由调用方构造并传入，包含用户名等基本信息
     */
    public void add(Admin admin) {
        // 第一步：根据用户名查询数据库，检查该用户名是否已被注册
        Admin dbAdmin = adminMapper.selectByUsername(admin.getUsername());
        // 第二步：若查询结果不为null，说明用户名已存在，抛出业务异常中止后续操作
        if (ObjectUtil.isNotNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        // 第三步：如果调用方未设置密码（密码字段为空），则设置默认密码为"admin"
        if (ObjectUtil.isEmpty(admin.getPassword())) {
            admin.setPassword("admin");
        }
        // 第四步：如果调用方未设置姓名（姓名字段为空），则用用户名作为默认姓名
        if (ObjectUtil.isEmpty(admin.getName())) {
            admin.setName(admin.getUsername());
        }
        // 第五步：设置角色为"管理员"，这是管理员账号的默认角色标识
        admin.setRole("管理员");
        // 第六步：调用Mapper层将管理员实体插入到admin数据库表中
        adminMapper.insert(admin);
    }

    /**
     * 根据ID删除管理员
     * @param id 管理员ID（主键），对应admin表中的自增id字段
     */
    public void deleteById(Integer id) {
        // 直接调用Mapper的deleteById方法，根据主键ID从admin表中删除对应记录
        adminMapper.deleteById(id);
    }

    /**
     * 根据ID更新管理员信息（部分字段更新）
     * @param admin 包含更新字段的管理员实体对象，其中id字段用于定位要更新的记录，其余非null字段将被更新
     */
    public void updateById(Admin admin) {
        // 调用Mapper层updateById方法，按主键ID更新管理员信息（MyBatis会动态生成UPDATE SET语句）
        adminMapper.updateById(admin);
    }

    /**
     * 根据ID查询单个管理员
     * @param id 管理员ID（主键）
     * @return 管理员实体对象，如果ID对应的记录不存在则返回null
     */
    public Admin selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单个管理员记录
        return adminMapper.selectById(id);
    }

    /**
     * 根据条件查询所有管理员列表（无条件时查全部）
     * @param admin 查询条件实体，支持多字段组合查询（如按用户名、角色等筛选），传null或空对象则查全部
     * @return 管理员实体列表，无匹配结果时返回空列表（非null）
     */
    public List<Admin> selectAll(Admin admin) {
        // 调用Mapper层selectAll方法，MyBatis会根据admin对象中非空字段动态生成WHERE条件
        return adminMapper.selectAll(admin);
    }

    /**
     * 分页查询管理员列表
     * 使用PageHelper分页插件实现物理分页，仅查询当前页数据而非全量数据
     * @param admin 查询条件对象，用于筛选管理员
     * @param pageNum 当前页码（从1开始），表示要查询第几页的数据
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象，包含以下关键信息：
     *         - list: 当前页的数据列表
     *         - total: 符合查询条件的总记录数
     *         - pages: 总页数（total/pageSize向上取整）
     *         - pageNum: 当前页码
     *         - pageSize: 每页条数
     */
    public PageInfo<Admin> selectPage(Admin admin, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 会将页码和每页条数存入ThreadLocal，
        // 当紧随其后的第一个SQL查询执行时，MyBatis拦截器会自动在SQL末尾追加LIMIT子句实现物理分页
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（注意：此查询会被PageHelper拦截并自动添加分页逻辑）
        List<Admin> list = adminMapper.selectAll(admin);
        // PageInfo.of 将查询结果列表包装为PageInfo对象，自动计算并填充total、pages等分页元数据
        return PageInfo.of(list);
    }

    /**
     * 管理员登录验证
     * 先根据用户名从数据库查询管理员记录，再比对密码是否匹配
     * @param account 账户对象，由前端传入，包含username（用户名）和password（密码）两个字段
     * @return 验证通过返回数据库中完整的管理员账户信息（Account类型，实际是Admin实体，Admin继承自Account）
     * @throws CustomException 用户名不存在时抛出"用户不存在"异常；密码不匹配时抛出"账号或密码错误"异常
     */
    public Account login(Account account) {
        // 第一步：根据用户名从数据库查询管理员记录
        Account dbAdmin = adminMapper.selectByUsername(account.getUsername());
        // 第二步：如果查询结果为null，说明用户名不存在，抛出异常
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        // 第三步：比对用户输入的密码与数据库中存储的密码是否一致
        if (!account.getPassword().equals(dbAdmin.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        // 第四步：验证通过，返回数据库中的管理员信息（后续可能存入Session/Token）
        return dbAdmin;
    }

    /**
     * 修改管理员密码
     * 需要先验证原密码是否正确，验证通过后才允许设置新密码（防止未授权密码修改）
     * @param account 账户对象，包含username（用户名）、password（原密码）、newPassword（新密码）三个字段
     * @throws CustomException 用户不存在或原密码验证失败时抛出异常
     */
    public void updatePassword(Account account) {
        // 第一步：根据用户名查询管理员记录，确认用户是否存在
        Admin dbAdmin = adminMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbAdmin)) {
            throw new CustomException("用户不存在");
        }
        // 第二步：验证原密码是否正确（只有原密码正确才允许修改）
        if (!account.getPassword().equals(dbAdmin.getPassword())) {
            throw new CustomException("原密码错误");
        }
        // 第三步：将新密码设置到管理员实体对象中
        dbAdmin.setPassword(account.getNewPassword());
        // 第四步：将更新后的管理员实体（含新密码）持久化到数据库
        adminMapper.updateById(dbAdmin);
    }

}
