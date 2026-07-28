// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入OrderDetail实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.OrderDetail;
// 导入MyBatis的@Delete注解，用于在方法上直接声明SQL删除语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Delete;
// 导入MyBatis的@Select注解，用于在方法上直接声明SQL查询语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Select;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作order_detail（订单明细）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与OrderDetailMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.OrderDetailMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - deleteByOrderId方法使用@Delete注解直接在方法上定义SQL，其余方法的SQL由XML映射文件提供
 * - OrderDetail表用于存储订单中的商品明细信息，一个订单（orders表）可包含多条订单明细（order_detail表），形成一对多关系
 * - 每条订单明细记录包含：所属订单ID、商品ID、购买数量、商品单价、小计金额等
 */
public interface OrderDetailMapper {

    /**
     * 新增订单明细记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.OrderDetail">
     * 通过OrderDetailMapper.xml中配置的INSERT SQL语句，将OrderDetail对象的所有属性值插入到order_detail表中
     * @param orderDetail 要新增的订单明细实体对象，包含订单ID、商品ID、商品名称、购买数量、单价、小计金额等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(OrderDetail orderDetail);

    /**
     * 根据主键ID删除订单明细记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过OrderDetailMapper.xml中配置的DELETE SQL语句，删除order_detail表中指定ID的记录
     * @param id 要删除的订单明细记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新订单明细记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.OrderDetail">
     * 通过OrderDetailMapper.xml中配置的UPDATE SQL语句，将OrderDetail对象中非空的属性更新到order_detail表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * @param orderDetail 要更新的订单明细实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(OrderDetail orderDetail);

    /**
     * 根据主键ID查询订单明细记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.OrderDetail">
     * 通过OrderDetailMapper.xml中配置的SELECT SQL语句，根据主键id查询order_detail表中的单条记录
     * MyBatis会自动将查询结果的列值映射到OrderDetail实体对象的对应属性中
     * @param id 要查询的订单明细记录的主键ID值
     * @return 查询到的OrderDetail实体对象，如果未找到则返回null
     */
    OrderDetail selectById(Integer id);

    /**
     * 查询所有订单明细记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.OrderDetail" resultType="com.wll.common.entity.OrderDetail">
     * 通过OrderDetailMapper.xml中配置的SELECT SQL语句，动态查询order_detail表中的记录
     * 如果传入的OrderDetail对象中某些属性不为空，则作为WHERE条件进行筛选（如按订单ID查询该订单的所有商品明细）
     * 如果所有属性都为空，则返回order_detail表中的所有记录
     * @param orderDetail 包含筛选条件的OrderDetail实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的OrderDetail实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<OrderDetail> selectAll(OrderDetail orderDetail);

    /**
     * 根据订单ID删除该订单的所有明细记录（使用注解方式定义SQL）
     * @Delete注解：声明该方法执行一条DELETE类型的SQL删除语句
     * 注解中的SQL："delete from `order_detail` where order_id = #{orderId}"
     * 表示删除order_detail表中所有order_id等于传入参数值的记录
     * #{orderId}是MyBatis的参数占位符，MyBatis使用PreparedStatement方式安全地设置参数值，防止SQL注入
     * 该方法没有使用@Param注解，因为只有一个Integer类型参数，MyBatis可以直接通过参数名匹配
     * 注意：order_detail表名使用反引号``包裹，因为表名中包含下划线，使用反引号是好习惯
     * 此方法通常在取消订单或删除订单时调用，用于级联清理订单下的所有商品明细记录
     * @param orderId 要删除明细的订单ID值（删除该订单下的所有明细条目）
     */
    @Delete("delete from `order_detail` where order_id = #{orderId}")
    void deleteByOrderId(Integer orderId);

}
