// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入Collect实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.Collect;
// 导入MyBatis的@Select注解，用于在方法上直接声明SQL查询语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Select;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作collect（商品收藏）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与CollectMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.CollectMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - 没有标注@Select等注解的方法，其SQL语句由对应的XML映射文件提供
 * - Collect表用于存储用户收藏商品的数据，记录用户和商品之间的收藏关系
 */
public interface CollectMapper {

    /**
     * 新增收藏记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.Collect">
     * 通过CollectMapper.xml中配置的INSERT SQL语句，将Collect对象的所有属性值插入到collect表中
     * @param collect 要新增的收藏实体对象，包含用户ID、商品ID、收藏时间等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(Collect collect);

    /**
     * 根据主键ID删除收藏记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过CollectMapper.xml中配置的DELETE SQL语句，删除collect表中指定ID的记录
     * @param id 要删除的收藏记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新收藏记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.Collect">
     * 通过CollectMapper.xml中配置的UPDATE SQL语句，将Collect对象中非空的属性更新到collect表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * @param collect 要更新的收藏实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(Collect collect);

    /**
     * 根据主键ID查询收藏记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.Collect">
     * 通过CollectMapper.xml中配置的SELECT SQL语句，根据主键id查询collect表中的单条记录
     * MyBatis会自动将查询结果的列值映射到Collect实体对象的对应属性中
     * @param id 要查询的收藏记录的主键ID值
     * @return 查询到的Collect实体对象，如果未找到则返回null
     */
    Collect selectById(Integer id);

    /**
     * 查询所有收藏记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.Collect" resultType="com.wll.common.entity.Collect">
     * 通过CollectMapper.xml中配置的SELECT SQL语句，动态查询collect表中的记录
     * 如果传入的Collect对象中某些属性不为空，则作为WHERE条件进行筛选（如按用户ID查询某用户的所有收藏）
     * 如果所有属性都为空，则返回collect表中的所有记录
     * @param collect 包含筛选条件的Collect实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的Collect实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<Collect> selectAll(Collect collect);

}
