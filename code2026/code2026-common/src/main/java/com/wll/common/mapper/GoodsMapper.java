// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入Goods实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.Goods;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作goods（商品）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与GoodsMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.GoodsMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - 本接口中所有方法的SQL语句均由GoodsMapper.xml映射文件提供（无注解SQL）
 * - Goods表是系统的核心数据表，用于存储商品的完整信息，包括名称、价格、库存、描述、图片、分类、所属店铺等
 */
public interface GoodsMapper {

    /**
     * 新增商品记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.Goods">
     * 通过GoodsMapper.xml中配置的INSERT SQL语句，将Goods对象的所有属性值插入到goods表中
     * 通常MyBatis会配置useGeneratedKeys="true"和keyProperty="id"，自动将数据库生成的主键值回填到Goods对象的id属性中
     * @param goods 要新增的商品实体对象，包含商品名称、价格、库存、描述、图片、分类ID、店铺ID等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(Goods goods);

    /**
     * 根据主键ID删除商品记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过GoodsMapper.xml中配置的DELETE SQL语句，删除goods表中指定ID的记录
     * 注意：删除商品时可能需要级联处理关联数据（如购物车中的对应记录、收藏记录等）
     * @param id 要删除的商品记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新商品记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.Goods">
     * 通过GoodsMapper.xml中配置的UPDATE SQL语句，将Goods对象中非空的属性更新到goods表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段，避免将未设置的字段覆盖为null
     * @param goods 要更新的商品实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(Goods goods);

    /**
     * 根据主键ID查询商品记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.Goods">
     * 通过GoodsMapper.xml中配置的SELECT SQL语句，根据主键id查询goods表中的单条记录
     * MyBatis会自动将查询结果的列值映射到Goods实体对象的对应属性中
     * XML中可能使用resultMap来配置复杂的关联映射（如关联查询分类名称、店铺名称等）
     * @param id 要查询的商品记录的主键ID值
     * @return 查询到的Goods实体对象，如果未找到则返回null
     */
    Goods selectById(Integer id);

    /**
     * 查询所有商品记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.Goods" resultType="com.wll.common.entity.Goods">
     * 通过GoodsMapper.xml中配置的SELECT SQL语句，动态查询goods表中的记录
     * 如果传入的Goods对象中某些属性不为空，则作为WHERE条件进行筛选（如按分类ID、店铺ID、商品名称模糊搜索、价格区间等）
     * 如果所有属性都为空，则返回goods表中的所有记录
     * 此方法通常也支持分页查询，配合PageHelper等分页插件使用
     * @param goods 包含筛选条件的Goods实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的Goods实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<Goods> selectAll(Goods goods);

    /**
     * 根据关键词查询商品搜索建议列表
     * XML中对应：<select id="selectSuggestions" parameterType="java.lang.String" resultType="java.lang.String">
     * 通过GoodsMapper.xml中配置的SELECT SQL语句，根据关键词查询goods表中的商品名称
     * 该方法用于实现搜索框的自动补全/搜索建议功能（autocomplete），用户在搜索框输入时实时展示匹配的商品名称建议
     * 通常SQL使用LIKE模糊查询：SELECT DISTINCT name FROM goods WHERE name LIKE CONCAT('%', #{keyword}, '%')
     * @param keyword 用户输入的关键词（字符串类型），用于模糊匹配商品名称
     * @return 匹配的商品名称字符串列表（List<String>），每个元素是一条建议文本，无结果时返回空列表
     */
    List<String> selectSuggestions(String keyword);
}
