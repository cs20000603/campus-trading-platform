// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入Orders实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.Orders;
// 导入MyBatis的@Select注解，用于在方法上直接声明SQL查询语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Select;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作orders（订单）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与OrdersMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.OrdersMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - 没有标注@Select等注解的方法，其SQL语句由对应的XML映射文件提供
 * - Orders表是订单系统的核心数据表，存储订单主表信息（订单号、用户ID、总金额、支付状态、收货地址、下单时间等）
 * - 一个订单（orders表的一条记录）可以包含多个订单明细（order_detail表的多条记录），形成一对多的主从表关系
 */
public interface OrdersMapper {

    /**
     * 新增订单记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.Orders">
     * 通过OrdersMapper.xml中配置的INSERT SQL语句，将Orders对象的所有属性值插入到orders表中
     * 通常MyBatis会配置useGeneratedKeys="true"和keyProperty="id"，自动将数据库生成的主键值回填到Orders对象的id属性中
     * @param orders 要新增的订单实体对象，包含用户ID、订单总金额、支付状态、收货地址、下单时间等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(Orders orders);

    /**
     * 根据主键ID删除订单记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过OrdersMapper.xml中配置的DELETE SQL语句，删除orders表中指定ID的记录
     * 注意：删除订单时通常需要级联删除其关联的订单明细记录（order_detail表）
     * @param id 要删除的订单记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新订单记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.Orders">
     * 通过OrdersMapper.xml中配置的UPDATE SQL语句，将Orders对象中非空的属性更新到orders表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * 常见场景：更新订单支付状态（待支付->已支付）、更新物流信息、更新收货地址等
     * @param orders 要更新的订单实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(Orders orders);

    /**
     * 根据主键ID查询订单记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.Orders">
     * 通过OrdersMapper.xml中配置的SELECT SQL语句，根据主键id查询orders表中的单条记录
     * MyBatis会自动将查询结果的列值映射到Orders实体对象的对应属性中
     * XML中可能使用resultMap来配置一对多关联映射（关联查询该订单下的所有order_detail明细记录）
     * @param id 要查询的订单记录的主键ID值
     * @return 查询到的Orders实体对象，如果未找到则返回null
     */
    Orders selectById(Integer id);

    /**
     * 查询所有订单记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.Orders" resultType="com.wll.common.entity.Orders">
     * 通过OrdersMapper.xml中配置的SELECT SQL语句，动态查询orders表中的记录
     * 如果传入的Orders对象中某些属性不为空，则作为WHERE条件进行筛选（如按用户ID查询某用户的所有订单、按订单状态筛选、按时间范围筛选等）
     * 如果所有属性都为空，则返回orders表中的所有记录
     * 此方法通常也支持分页查询，配合PageHelper等分页插件使用，用于订单列表的分页展示
     * @param orders 包含筛选条件的Orders实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的Orders实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<Orders> selectAll(Orders orders);

}
