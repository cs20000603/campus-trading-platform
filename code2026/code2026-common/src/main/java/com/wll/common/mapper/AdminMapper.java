// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入Admin实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.Admin;
// 导入MyBatis的@Select注解，用于在方法上直接声明SQL查询语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Select;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作admin（管理员）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与AdminMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.AdminMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - 没有标注@Select等注解的方法，其SQL语句由对应的XML映射文件提供
 */
public interface AdminMapper {

    /**
     * 新增管理员记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.Admin">
     * 通过AdminMapper.xml中配置的INSERT SQL语句，将Admin对象的所有属性值插入到admin表中
     * @param admin 要新增的管理员实体对象，包含username、password等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(Admin admin);

    /**
     * 根据主键ID删除管理员记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过AdminMapper.xml中配置的DELETE SQL语句，删除admin表中指定ID的记录
     * @param id 要删除的管理员记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新管理员记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.Admin">
     * 通过AdminMapper.xml中配置的UPDATE SQL语句，将Admin对象中非空的属性更新到admin表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * @param admin 要更新的管理员实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(Admin admin);

    /**
     * 根据主键ID查询管理员记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.Admin">
     * 通过AdminMapper.xml中配置的SELECT SQL语句，根据主键id查询admin表中的单条记录
     * MyBatis会自动将查询结果的列值映射到Admin实体对象的对应属性中
     * @param id 要查询的管理员记录的主键ID值
     * @return 查询到的Admin实体对象，如果未找到则返回null
     */
    Admin selectById(Integer id);

    /**
     * 查询所有管理员记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.Admin" resultType="com.wll.common.entity.Admin">
     * 通过AdminMapper.xml中配置的SELECT SQL语句，动态查询admin表中的记录
     * 如果传入的Admin对象中某些属性不为空，则作为WHERE条件进行筛选
     * 如果所有属性都为空，则返回admin表中的所有记录
     * @param admin 包含筛选条件的Admin实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的Admin实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<Admin> selectAll(Admin admin);

    /**
     * 根据用户名查询管理员记录（使用注解方式定义SQL）
     * @Select注解：声明该方法执行一条SELECT类型的SQL查询语句
     * 注解中的SQL："select * from `admin` where username = #{username}" 表示查询admin表中username字段等于传入参数值的记录
     * #{username}是MyBatis的参数占位符，会自动从方法参数中获取值并安全地拼接到SQL中（使用PreparedStatement方式，防止SQL注入）
     * 该方法没有使用@Param注解，因为只有一个String类型参数，MyBatis可直接通过参数名匹配
     * 注意：admin表名使用反引号``包裹，因为admin是MySQL的保留关键字，需要使用反引号避免语法冲突
     * @param username 要查询的管理员用户名（登录账号）
     * @return 查询到的Admin实体对象，如果用户名不存在则返回null
     */
    @Select("select * from `admin` where username = #{username}")
    Admin selectByUsername(String username);

}
