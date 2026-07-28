// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入User实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.User;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作user（用户）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与UserMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.UserMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - 本接口中所有方法的SQL语句均由UserMapper.xml映射文件提供（无注解SQL）
 * - User表是系统的用户核心数据表，存储用户的账号信息和个人资料
 * - 包括用户基本信息（用户名、密码、手机号、头像等）、账户余额、注册时间、微信OpenID等
 * - 支持多种方式查询用户：按ID、按用户名、按手机号、按微信OpenID
 */
public interface UserMapper {

    /**
     * 查询所有用户记录（支持按名称模糊搜索）
     * XML中对应：<select id="selectAll" parameterType="java.lang.String" resultType="com.wll.common.entity.User">
     * 通过UserMapper.xml中配置的SELECT SQL语句，查询user表中的记录
     * 参数name用于按用户名进行模糊匹配搜索（LIKE查询）
     * 当name为null或空字符串时，通常返回所有用户记录
     * @param name 用于模糊匹配的用户名字符串（可为空，为空时查询所有用户）
     * @return 符合条件的User实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<User> selectAll(String name);

    /**
     * 根据主键ID删除用户记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过UserMapper.xml中配置的DELETE SQL语句，删除user表中指定ID的记录
     * 返回类型为void表示该方法不需要返回受影响的行数
     * 注意：删除用户时通常需要级联处理关联数据（该用户的订单、收藏、购物车、评论等）
     * @param id 要删除的用户记录的主键ID值
     */
    void deleteById(Integer id);

    /**
     * 新增用户记录（用户注册）
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.User">
     * 通过UserMapper.xml中配置的INSERT SQL语句，将User对象的所有属性值插入到user表中
     * 返回类型为void表示该方法不需要返回受影响的行数
     * 通常MyBatis会配置useGeneratedKeys="true"和keyProperty="id"，自动将数据库生成的主键值回填到User对象的id属性中
     * @param user 要新增的用户实体对象，包含用户名、密码、手机号、头像等注册信息
     */
    void insert(User user);

    /**
     * 根据用户名查询用户记录（用于登录验证和唯一性校验）
     * XML中对应：<select id="selectByUsername" parameterType="java.lang.String" resultType="com.wll.common.entity.User">
     * 通过UserMapper.xml中配置的SELECT SQL语句，根据username字段查询user表中的用户记录
     * 用户名在系统中通常是唯一的，此方法用于：
     * 1. 用户登录时根据用户名查找用户信息，验证密码是否正确
     * 2. 用户注册时检查用户名是否已被占用
     * @param username 要查询的用户名字符串（登录名）
     * @return 查询到的User实体对象，如果用户名不存在则返回null
     */
    User selectByUsername(String username);

    /**
     * 根据主键ID更新用户信息
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.User">
     * 通过UserMapper.xml中配置的UPDATE SQL语句，将User对象中非空的属性更新到user表中
     * 返回类型为void表示该方法不需要返回受影响的行数
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * 常见场景：修改个人资料（头像、昵称、手机号等）、修改密码、更新账户余额
     * @param user 要更新的用户实体对象，必须包含有效的id属性值用于定位要更新的记录
     */
    void updateById(User user);

    /**
     * 根据主键ID查询用户记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.User">
     * 通过UserMapper.xml中配置的SELECT SQL语句，根据主键id查询user表中的单条记录
     * MyBatis会自动将查询结果的列值映射到User实体对象的对应属性中
     * @param id 要查询的用户记录的主键ID值
     * @return 查询到的User实体对象，如果未找到则返回null
     */
    User selectById(Integer id);

    /**
     * 根据手机号查询用户记录（用于手机号登录和唯一性校验）
     * XML中对应：<select id="selectByPhone" parameterType="java.lang.String" resultType="com.wll.common.entity.User">
     * 通过UserMapper.xml中配置的SELECT SQL语句，根据phone字段查询user表中的用户记录
     * 手机号在系统中通常是唯一的，此方法用于：
     * 1. 用户通过手机号+验证码方式登录时，查找用户信息
     * 2. 用户注册/绑定手机号时，检查手机号是否已被使用
     * @param phone 要查询的手机号字符串
     * @return 查询到的User实体对象，如果该手机号未注册则返回null
     */
    User selectByPhone(String phone);

    /**
     * 根据微信OpenID查询用户记录（用于微信登录关联）
     * XML中对应：<select id="selectByOpenid" parameterType="java.lang.String" resultType="com.wll.common.entity.User">
     * 通过UserMapper.xml中配置的SELECT SQL语句，根据openid字段查询user表中的用户记录
     * OpenID是微信用户在当前小程序/公众号下的唯一标识，此方法用于：
     * 1. 微信授权登录时，根据OpenID查找已绑定的用户账号，实现自动登录
     * 2. 首次微信登录时，如果未找到记录，则创建新用户并绑定OpenID
     * @param openid 微信平台返回的用户OpenID字符串（微信用户在当前应用下的唯一标识）
     * @return 查询到的User实体对象，如果该微信用户尚未绑定账号则返回null
     */
    User selectByOpenid(String openid);

}
