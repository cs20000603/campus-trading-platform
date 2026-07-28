// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入Cart实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.Cart;
// 导入MyBatis的@Delete注解，用于在方法上直接声明SQL删除语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Delete;
// 导入MyBatis的@Param注解，用于为方法参数指定在SQL中引用的参数名称
import org.apache.ibatis.annotations.Param;
// 导入MyBatis的@Select注解，用于在方法上直接声明SQL查询语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Select;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作cart（购物车）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与CartMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.CartMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - 部分方法使用注解方式（@Select/@Delete）直接在方法上定义SQL，无需XML配置
 * - 剩余没有标注注解的方法，其SQL语句由对应的XML映射文件提供
 * - Cart表用于存储用户的购物车数据，记录用户添加的商品及数量等信息
 */
public interface CartMapper {

    /**
     * 新增购物车记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.Cart">
     * 通过CartMapper.xml中配置的INSERT SQL语句，将Cart对象的所有属性值插入到cart表中
     * @param cart 要新增的购物车实体对象，包含用户ID、商品ID、数量等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(Cart cart);

    /**
     * 根据主键ID删除购物车记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过CartMapper.xml中配置的DELETE SQL语句，删除cart表中指定ID的记录
     * @param id 要删除的购物车记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新购物车记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.Cart">
     * 通过CartMapper.xml中配置的UPDATE SQL语句，将Cart对象中非空的属性更新到cart表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * @param cart 要更新的购物车实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(Cart cart);

    /**
     * 根据主键ID查询购物车记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.Cart">
     * 通过CartMapper.xml中配置的SELECT SQL语句，根据主键id查询cart表中的单条记录
     * MyBatis会自动将查询结果的列值映射到Cart实体对象的对应属性中
     * @param id 要查询的购物车记录的主键ID值
     * @return 查询到的Cart实体对象，如果未找到则返回null
     */
    Cart selectById(Integer id);

    /**
     * 查询所有购物车记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.Cart" resultType="com.wll.common.entity.Cart">
     * 通过CartMapper.xml中配置的SELECT SQL语句，动态查询cart表中的记录
     * 如果传入的Cart对象中某些属性不为空，则作为WHERE条件进行筛选
     * 如果所有属性都为空，则返回cart表中的所有记录
     * @param cart 包含筛选条件的Cart实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的Cart实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<Cart> selectAll(Cart cart);

    /**
     * 根据商品ID和用户ID查询购物车记录（使用注解方式定义SQL）
     * @Select注解：声明该方法执行一条SELECT类型的SQL查询语句
     * 注解中的SQL："select * from cart where goods_id = #{goodsId} and user_id = #{userId}"
     * 表示查询cart表中goods_id和user_id同时匹配传入参数值的记录
     * #{goodsId}和#{userId}是MyBatis的参数占位符，MyBatis使用PreparedStatement方式安全地设置参数值，防止SQL注入
     * @Param("goodsId")注解：将方法参数goodsId绑定到SQL中的#{goodsId}占位符
     * @Param("userId")注解：将方法参数userId绑定到SQL中的#{userId}占位符
     * 当方法有多个参数时，必须使用@Param注解为每个参数指定名称，否则MyBatis无法正确识别参数映射
     * 该方法用于判断某个用户是否已将某个商品添加到购物车，避免重复添加
     * @param goodsId 要查询的商品ID值
     * @param userId 要查询的用户ID值
     * @return 查询到的Cart实体对象，如果该用户没有将此商品加入购物车则返回null
     */
    @Select("select * from cart where goods_id = #{goodsId} and user_id = #{userId}")
    Cart selectByGoodsIdAndUserId(@Param("goodsId") Integer goodsId, @Param("userId") Integer userId);

    /**
     * 根据商品ID删除购物车记录（使用注解方式定义SQL）
     * @Delete注解：声明该方法执行一条DELETE类型的SQL删除语句
     * 注解中的SQL："delete from cart where goods_id = #{goodsId}"
     * 表示删除cart表中所有goods_id等于传入参数值的记录
     * #{goodsId}是MyBatis的参数占位符，MyBatis使用PreparedStatement方式安全地设置参数值，防止SQL注入
     * 该方法没有使用@Param注解，因为只有一个Integer类型参数，MyBatis可以直接通过参数名匹配
     * 此方法通常在商品被下架或删除时调用，用于清理该商品在购物车中的所有关联数据
     * @param goodsId 要删除购物车记录的商品ID值（删除所有包含该商品的购物车条目）
     */
    @Delete("delete from cart where goods_id = #{goodsId}")
    void deleteByGoodsId(Integer goodsId);

}
