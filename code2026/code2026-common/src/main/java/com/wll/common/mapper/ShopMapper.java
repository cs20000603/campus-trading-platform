// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入Shop实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.Shop;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作shop（店铺）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与ShopMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.ShopMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - 本接口中所有方法的SQL语句均由ShopMapper.xml映射文件提供（无注解SQL）
 * - Shop表用于存储商家店铺信息，包括店铺名称、店铺类型、店铺简介、店主用户ID、店铺Logo、联系电话、营业状态等
 * - 这是一个多商户电商平台架构：每个用户可以开设自己的店铺，店铺下可管理多个商品
 */
public interface ShopMapper {

    /**
     * 新增店铺记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.Shop">
     * 通过ShopMapper.xml中配置的INSERT SQL语句，将Shop对象的所有属性值插入到shop表中
     * 通常MyBatis会配置useGeneratedKeys="true"和keyProperty="id"，自动将数据库生成的主键值回填到Shop对象的id属性中
     * @param shop 要新增的店铺实体对象，包含店铺名称、店铺类型、简介、店主用户ID、Logo图片、联系电话等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(Shop shop);

    /**
     * 根据主键ID删除店铺记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过ShopMapper.xml中配置的DELETE SQL语句，删除shop表中指定ID的记录
     * 注意：删除店铺时可能需要级联处理关联数据（该店铺下的所有商品、订单等）
     * @param id 要删除的店铺记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新店铺记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.Shop">
     * 通过ShopMapper.xml中配置的UPDATE SQL语句，将Shop对象中非空的属性更新到shop表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * 常见场景：更新店铺信息（名称、简介、Logo、联系方式等）、切换营业状态（营业中/休息中）
     * @param shop 要更新的店铺实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(Shop shop);

    /**
     * 根据主键ID查询店铺记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.Shop">
     * 通过ShopMapper.xml中配置的SELECT SQL语句，根据主键id查询shop表中的单条记录
     * MyBatis会自动将查询结果的列值映射到Shop实体对象的对应属性中
     * @param id 要查询的店铺记录的主键ID值
     * @return 查询到的Shop实体对象，如果未找到则返回null
     */
    Shop selectById(Integer id);

    /**
     * 查询所有店铺记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.Shop" resultType="com.wll.common.entity.Shop">
     * 通过ShopMapper.xml中配置的SELECT SQL语句，动态查询shop表中的记录
     * 如果传入的Shop对象中某些属性不为空，则作为WHERE条件进行筛选（如按店铺类型、店铺名称模糊搜索、营业状态等）
     * 如果所有属性都为空，则返回shop表中的所有记录
     * 此方法通常用于首页店铺列表展示，配合分页插件实现分页加载
     * @param shop 包含筛选条件的Shop实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的Shop实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<Shop> selectAll(Shop shop);

    /**
     * 根据店主用户ID查询其拥有的店铺
     * XML中对应：<select id="selectByOwnerId" parameterType="java.lang.Integer" resultType="com.wll.common.entity.Shop">
     * 通过ShopMapper.xml中配置的SELECT SQL语句，根据owner_id（店主用户ID）查询shop表中的记录
     * 此方法用于查询某个用户开设的店铺，通常一个用户只能开设一个店铺（一对一关系）
     * @param ownerId 店主用户的ID值，用于查询该用户拥有的店铺
     * @return 查询到的Shop实体对象，如果该用户还未开店则返回null
     */
    Shop selectByOwnerId(Integer ownerId);

    /**
     * 查询所有不同的店铺类型列表（用于筛选/分类展示）
     * XML中对应：<select id="selectDistinctTypes" resultType="java.lang.String">
     * 通过ShopMapper.xml中配置的SELECT SQL语句，查询shop表中所有不重复的店铺类型名称
     * SQL通常为：SELECT DISTINCT type FROM shop
     * DISTINCT关键字用于去除重复值，确保返回的每个店铺类型只出现一次
     * 此方法用于前端店铺类型筛选下拉框的数据源，或首页按类型分类展示店铺
     * @return 所有不重复的店铺类型名称列表（List<String>），无结果时返回空列表
     */
    List<String> selectDistinctTypes();
}
