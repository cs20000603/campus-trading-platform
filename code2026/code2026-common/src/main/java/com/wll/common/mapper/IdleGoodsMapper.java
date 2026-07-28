// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入IdleGoods实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.IdleGoods;
// 导入MyBatis的@Update注解，用于在方法上直接声明SQL更新语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Update;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作idle_goods（闲置物品）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与IdleGoodsMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.IdleGoodsMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - incrementViews方法使用@Update注解直接在方法上定义SQL，其余方法的SQL由XML映射文件提供
 * - IdleGoods表用于存储闲置交易模块的商品信息，包括闲置物品名称、描述、图片、价格、发布用户、浏览次数等
 */
public interface IdleGoodsMapper {

    /**
     * 新增闲置物品记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.IdleGoods">
     * 通过IdleGoodsMapper.xml中配置的INSERT SQL语句，将IdleGoods对象的所有属性值插入到idle_goods表中
     * @param idleGoods 要新增的闲置物品实体对象，包含物品名称、描述、图片、价格、发布用户ID、分类等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(IdleGoods idleGoods);

    /**
     * 根据主键ID删除闲置物品记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过IdleGoodsMapper.xml中配置的DELETE SQL语句，删除idle_goods表中指定ID的记录
     * @param id 要删除的闲置物品记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新闲置物品记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.IdleGoods">
     * 通过IdleGoodsMapper.xml中配置的UPDATE SQL语句，将IdleGoods对象中非空的属性更新到idle_goods表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * @param idleGoods 要更新的闲置物品实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(IdleGoods idleGoods);

    /**
     * 根据主键ID查询闲置物品记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.IdleGoods">
     * 通过IdleGoodsMapper.xml中配置的SELECT SQL语句，根据主键id查询idle_goods表中的单条记录
     * MyBatis会自动将查询结果的列值映射到IdleGoods实体对象的对应属性中
     * @param id 要查询的闲置物品记录的主键ID值
     * @return 查询到的IdleGoods实体对象，如果未找到则返回null
     */
    IdleGoods selectById(Integer id);

    /**
     * 查询所有闲置物品记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.IdleGoods" resultType="com.wll.common.entity.IdleGoods">
     * 通过IdleGoodsMapper.xml中配置的SELECT SQL语句，动态查询idle_goods表中的记录
     * 如果传入的IdleGoods对象中某些属性不为空，则作为WHERE条件进行筛选（如按发布用户ID、分类、状态等筛选）
     * 如果所有属性都为空，则返回idle_goods表中的所有记录
     * @param idleGoods 包含筛选条件的IdleGoods实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的IdleGoods实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<IdleGoods> selectAll(IdleGoods idleGoods);

    /**
     * 增加闲置物品的浏览次数（使用注解方式定义SQL）
     * @Update注解：声明该方法执行一条UPDATE类型的SQL更新语句
     * 注解中的SQL："update `idle_goods` set views = views + 1 where id = #{id}"
     * 表示将idle_goods表中指定id记录的views（浏览次数）字段值增加1
     * 使用SET views = views + 1的写法是原子操作，直接在数据库层面完成自增，避免了并发情况下的数据不一致问题
     * #{id}是MyBatis的参数占位符，MyBatis使用PreparedStatement方式安全地设置参数值，防止SQL注入
     * 注意：idle_goods表名使用反引号``包裹，因为其中包含下划线，虽然不是保留关键字但使用反引号是好习惯
     * 该方法在用户每次查看闲置物品详情时调用，用于统计物品的浏览热度
     * @param id 要增加浏览次数的闲置物品记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    @Update("update `idle_goods` set views = views + 1 where id = #{id}")
    int incrementViews(Integer id);
}
