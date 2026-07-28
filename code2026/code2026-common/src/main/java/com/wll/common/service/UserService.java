// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的ObjectUtil类，提供对象判空、判非空等工具方法（如isNull、isNotNull）
import cn.hutool.core.util.ObjectUtil;
// 导入Hutool工具库的StrUtil类，提供字符串判空、判非空等工具方法（如isBlank、isNotBlank）
import cn.hutool.core.util.StrUtil;
// 导入Account实体类，用于登录验证和密码修改时承载用户名、密码、新密码等字段（User类继承自Account）
import com.wll.common.entity.Account;
// 导入User实体类，对应数据库user表的ORM映射，包含用户名、密码、姓名、角色、余额、手机号等字段
import com.wll.common.entity.User;
// 导入自定义业务异常类CustomException，用于在业务校验失败时向调用方抛出可读的错误信息
import com.wll.common.exception.CustomException;
// 导入UserMapper数据访问接口，封装对user表的所有数据库CRUD操作及自定义查询（按用户名、手机号查询等）
import com.wll.common.mapper.UserMapper;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表、总记录数、总页数等分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件，被IoC容器管理
import org.springframework.stereotype.Service;

// 导入Java标准库的BigDecimal类，用于精确的货币金额运算（初始余额设为0时使用）
import java.math.BigDecimal;
// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;
// 导入Java标准库的Objects工具类，用于安全的对象比较（如Objects.equals替代==避免空指针）
import java.util.Objects;

/**
 * 用户业务处理服务
 * 负责用户的注册、登录、信息修改、密码管理、余额管理等核心业务
 * 支持多种登录方式：
 * - 用户名+密码登录（login(Account)）
 * - 用户名或手机号+密码登录（login(String, String)）
 * - 手机号快捷登录（loginByPhone，不校验密码）
 * 业务规则：
 * - 注册时校验用户名唯一性，自动设置默认密码(123)、默认姓名(取用户名)、默认角色("普通用户")、初始余额(0)
 * - 更新用户信息时校验手机号和用户名的唯一性（排除自身）
 * - 修改密码需要验证原密码（防止未授权修改）
 * - 重置密码无需验证原密码（用于管理员操作或忘记密码场景）
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class UserService {

    // @Resource 注解：按照名称从Spring容器中注入UserMapper Bean，用于执行user表的数据库CRUD操作
    @Resource
    private UserMapper userMapper;

    /**
     * 根据姓名模糊查询用户列表
     * @param name 用户姓名（支持模糊匹配，SQL中使用LIKE '%name%'）
     * @return 匹配的用户实体列表，无匹配结果时返回空列表（非null）
     */
    public List<User> selectByAll(String name) {
        // 调用Mapper层selectAll方法，传入name作为模糊查询条件
        return userMapper.selectAll(name);
    }

    /**
     * 分页查询用户列表
     * 使用PageHelper分页插件实现物理分页，支持按姓名模糊筛选
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @param name 用户姓名（模糊查询条件，传null或空字符串则查询全部）
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的用户数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<User> selectPage(Integer pageNum, Integer pageSize, String name) {
        // PageHelper.startPage 将分页参数存入ThreadLocal，拦截后第一个SQL自动添加LIMIT分页
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（自动分页）
        List<User> list = userMapper.selectAll(name);
        // 包装为PageInfo对象，自动填充分页统计信息
        return PageInfo.of(list);
    }

    /**
     * 根据ID删除用户
     * @param id 用户ID（主键），对应user表中的自增id字段
     */
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从user表中删除对应记录
        userMapper.deleteById(id);
    }

    /**
     * 新增用户（注册）
     * 完整流程：校验用户名唯一性 -> 设置默认密码 -> 设置默认姓名 -> 设置默认角色和余额 -> 插入数据库
     * @param user 用户实体对象，由调用方构造并传入，至少需包含username字段
     * @throws CustomException 如果用户名已被其他用户占用，抛出"新增失败！账号重复"异常
     */
    public void add(User user) {
        // 第一步：获取用户输入的用户名
        String username = user.getUsername();
        // 第二步：查询该用户名是否已在数据库中存在
        User dbUser = userMapper.selectByUsername(username);
        if (dbUser != null) {
            // 用户名已存在，抛出异常提示账号重复
            throw new CustomException("新增失败！账号重复");
        }
        // 第三步：如果调用方未设置密码（密码为空），则设置默认密码为"123"
        if (StrUtil.isBlank(user.getPassword())) {
            user.setPassword("123");
        }
        // 第四步：如果调用方未设置姓名（姓名为空），则用用户名作为默认姓名
        if (StrUtil.isBlank(user.getName())) {
            user.setName(user.getUsername());
        }
        // 第五步：设置默认角色为"普通用户"（区别于"merchant"商家角色）
        user.setRole("普通用户");
        // 第六步：设置初始账户余额为0（BigDecimal.ZERO，精确表示零元）
        user.setAccount(BigDecimal.ZERO);
        // 第七步：调用Mapper层insert方法，将用户实体插入到user数据库表中
        userMapper.insert(user);
    }

    /**
     * 更新用户信息（如修改姓名、手机号、头像等）
     * 更新前需校验手机号和用户名的唯一性（排除自身，即不能与其他用户冲突但可以与自己相同）
     * @param user 包含更新字段的用户实体对象，id字段定位要更新的记录
     * @throws CustomException 手机号被其他用户占用时抛出"该手机号已被其他用户使用"异常
     * @throws CustomException 用户名被其他用户占用时抛出"该用户名已被其他用户使用"异常
     */
    public void update(User user) {
        // 第一步：如果更新了手机号，校验手机号唯一性
        if (StrUtil.isNotBlank(user.getPhone())) {
            // 按手机号查询数据库中是否有其他用户使用该手机号
            User dbUser = userMapper.selectByPhone(user.getPhone());
            // 如果查到了记录，且该记录的用户ID与当前更新用户的ID不同，说明手机号被其他用户占用
            if (dbUser != null && !Objects.equals(dbUser.getId(), user.getId())) {
                throw new CustomException("该手机号已被其他用户使用");
            }
        }
        // 第二步：如果更新了用户名，校验用户名唯一性
        if (StrUtil.isNotBlank(user.getUsername())) {
            // 按用户名查询数据库中是否有其他用户使用该用户名
            User dbUser = userMapper.selectByUsername(user.getUsername());
            // 如果查到了记录，且该记录的用户ID与当前更新用户的ID不同，说明用户名被其他用户占用
            if (dbUser != null && !Objects.equals(dbUser.getId(), user.getId())) {
                throw new CustomException("该用户名已被其他用户使用");
            }
        }
        // 第三步：校验通过，执行数据库更新
        userMapper.updateById(user);
    }

    /**
     * 用户名+密码登录（供外部系统或API调用）
     * 按用户名精确查询，验证密码匹配
     * @param account 账户对象，包含username（用户名）和password（密码）
     * @return 验证通过返回完整的用户实体对象（继承自Account）
     * @throws CustomException 用户名不存在时抛出"用户不存在"异常
     * @throws CustomException 密码不匹配时抛出"账号或密码错误"异常
     */
    public Account login(Account account) {
        // 第一步：根据用户名从数据库查询用户记录
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            // 用户名不存在，抛出异常
            throw new CustomException("用户不存在");
        }
        // 第二步：比对用户输入的密码与数据库中存储的密码是否一致
        if (!account.getPassword().equals(dbUser.getPassword())) {
            // 密码不匹配，抛出异常
            throw new CustomException("账号或密码错误");
        }
        // 第三步：验证通过，返回数据库中的用户信息
        return dbUser;
    }

    /**
     * 用户名或手机号+密码登录（供前台页面调用）
     * 支持用户名或手机号作为登录凭证，比login(Account)更灵活
     * 设计差异：不抛异常而是返回null，由Controller层处理（提示"用户名或密码错误"）
     * @param username 登录凭证字符串，可以是用户名或手机号
     * @param password 密码明文
     * @return 验证通过返回用户信息，验证失败返回null（用户名/手机号不存在或密码不匹配）
     */
    public Account login(String username, String password) {
        // 第一步：先尝试按用户名查询
        User dbUser = userMapper.selectByUsername(username);
        if (ObjectUtil.isNull(dbUser)) {
            // 第二步：用户名查不到，尝试按手机号查询（支持手机号登录）
            dbUser = userMapper.selectByPhone(username);
        }
        // 第三步：如果按用户名和手机号都查不到，说明用户不存在
        if (ObjectUtil.isNull(dbUser)) {
            return null;  // 返回null让Controller层统一提示"用户名或密码错误"
        }
        // 第四步：验证密码是否匹配
        if (!password.equals(dbUser.getPassword())) {
            return null;  // 密码不匹配，同样返回null（不提示具体是用户名错还是密码错，增强安全性）
        }
        // 第五步：验证通过，返回用户信息
        return dbUser;
    }

    /**
     * 根据ID查询单个用户
     * @param id 用户ID（主键）
     * @return 用户实体对象，如果记录不存在则返回null
     */
    public User selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单个用户记录
        return userMapper.selectById(id);
    }

    /**
     * 手机号快捷登录（不校验密码）
     * 适用于验证码登录场景，仅凭手机号即可登录（调用方已通过短信验证码等方式验证过手机号归属）
     * @param phone 用户手机号字符串
     * @return 用户账户信息
     * @throws CustomException 用户不存在时抛出"用户不存在"异常（手机号未注册）
     */
    public Account loginByPhone(String phone) {
        // 第一步：根据手机号从数据库查询用户
        User dbUser = userMapper.selectByPhone(phone);
        if (ObjectUtil.isNull(dbUser)) {
            // 手机号未注册，抛出异常
            throw new CustomException("用户不存在");
        }
        // 第二步：查询成功，返回用户信息（不校验密码）
        return dbUser;
    }

    /**
     * 重置密码（管理员操作或忘记密码场景）
     * 直接设置新密码，不需要验证原密码
     * 适用于：管理员为用户重置密码、用户忘记密码后通过验证码等方式确认身份后重置密码
     * @param username 要重置密码的用户名
     * @param newPassword 新密码明文
     * @throws CustomException 用户名不存在时抛出"用户不存在"异常
     */
    public void resetPassword(String username, String newPassword) {
        // 第一步：根据用户名查询用户是否存在
        User dbUser = userMapper.selectByUsername(username);
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        // 第二步：直接设置新密码（不校验原密码）
        dbUser.setPassword(newPassword);
        // 第三步：将新密码持久化到数据库
        userMapper.updateById(dbUser);
    }

    /**
     * 修改密码（用户自主修改）
     * 需要验证原密码正确后才能设置新密码，防止未授权修改（如他人趁用户未退出时修改密码）
     * @param account 账户对象，包含 id（用户ID）、password（原密码）、newPassword（新密码）三个字段
     * @throws CustomException 用户不存在时抛出"用户不存在"异常
     * @throws CustomException 原密码不正确时抛出"原密码错误"异常
     */
    public void updatePassword(Account account) {
        // 第一步：根据用户ID查询用户（通过ID而非用户名，因为当前用户已登录知道自己的ID）
        User dbUser = userMapper.selectById(account.getId());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        // 第二步：验证原密码是否正确
        // 如果数据库中的密码不为空，且与原密码不匹配，说明原密码输入错误
        if (StrUtil.isNotBlank(dbUser.getPassword()) && !account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        // 第三步：原密码验证通过，设置新密码
        dbUser.setPassword(account.getNewPassword());
        // 第四步：将新密码持久化到数据库
        userMapper.updateById(dbUser);
    }

}
