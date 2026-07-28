// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入ChatMessage实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.ChatMessage;
// 导入MyBatis的@Select注解，用于在方法上直接声明SQL查询语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Select;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作chat_message（聊天消息）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与ChatMessageMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.ChatMessageMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - 本接口中所有方法的SQL语句均由ChatMessageMapper.xml映射文件提供
 * - ChatMessage表用于存储用户之间的聊天消息记录，包括发送者、接收者、消息内容、发送时间等
 */
public interface ChatMessageMapper {

    /**
     * 新增聊天消息记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.ChatMessage">
     * 通过ChatMessageMapper.xml中配置的INSERT SQL语句，将ChatMessage对象的所有属性值插入到chat_message表中
     * @param chatMessage 要新增的聊天消息实体对象，包含发送者ID、接收者ID、消息内容、发送时间等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(ChatMessage chatMessage);

    /**
     * 根据主键ID删除聊天消息记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过ChatMessageMapper.xml中配置的DELETE SQL语句，删除chat_message表中指定ID的记录
     * @param id 要删除的聊天消息记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据主键ID更新聊天消息记录
     * XML中对应：<update id="updateById" parameterType="com.wll.common.entity.ChatMessage">
     * 通过ChatMessageMapper.xml中配置的UPDATE SQL语句，将ChatMessage对象中非空的属性更新到chat_message表中
     * MyBatis会根据传入对象的属性值动态生成SET子句，只更新有值的字段
     * 常见场景：更新消息的已读状态（is_read字段）
     * @param chatMessage 要更新的聊天消息实体对象，必须包含有效的id属性值用于定位记录
     * @return 受影响的行数（int类型），大于0表示更新成功，0表示未找到对应记录
     */
    int updateById(ChatMessage chatMessage);

    /**
     * 根据主键ID查询聊天消息记录
     * XML中对应：<select id="selectById" parameterType="java.lang.Integer" resultType="com.wll.common.entity.ChatMessage">
     * 通过ChatMessageMapper.xml中配置的SELECT SQL语句，根据主键id查询chat_message表中的单条记录
     * MyBatis会自动将查询结果的列值映射到ChatMessage实体对象的对应属性中
     * @param id 要查询的聊天消息记录的主键ID值
     * @return 查询到的ChatMessage实体对象，如果未找到则返回null
     */
    ChatMessage selectById(Integer id);

    /**
     * 查询所有聊天消息记录（支持条件筛选）
     * XML中对应：<select id="selectAll" parameterType="com.wll.common.entity.ChatMessage" resultType="com.wll.common.entity.ChatMessage">
     * 通过ChatMessageMapper.xml中配置的SELECT SQL语句，动态查询chat_message表中的记录
     * 如果传入的ChatMessage对象中某些属性不为空，则作为WHERE条件进行筛选（如按发送者ID、接收者ID筛选）
     * 如果所有属性都为空，则返回chat_message表中的所有记录
     * @param chatMessage 包含筛选条件的ChatMessage实体对象（可为空，为空时查询所有记录）
     * @return 符合条件的ChatMessage实体对象列表（List集合），无结果时返回空列表而非null
     */
    List<ChatMessage> selectAll(ChatMessage chatMessage);
}
