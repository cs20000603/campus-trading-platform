// 声明当前Java类所属的包路径，用于组织和管理代码结构
package com.wll.common.mapper;

// 导入IdleMessage实体类，Mapper接口的方法参数和返回值需要使用该实体类型
import com.wll.common.entity.IdleMessage;
// 导入MyBatis的@Param注解，用于为方法参数指定在SQL中引用的参数名称
import org.apache.ibatis.annotations.Param;
// 导入MyBatis的@Update注解，用于在方法上直接声明SQL更新语句（注解方式），替代XML配置
import org.apache.ibatis.annotations.Update;

// 导入Java的List集合类，用于封装查询返回的多条记录
import java.util.List;

/**
 * 操作idle_message（闲置物品留言/私信）相关数据的Mapper接口
 * MyBatis Mapper接口是数据访问层（DAO层）的核心组件，用于定义数据库CRUD操作方法
 * 该接口与IdleMessageMapper.xml映射文件配合使用：
 * - 接口的完整类名（com.wll.common.mapper.IdleMessageMapper）对应XML中<mapper>标签的namespace属性值
 * - 接口中的每个方法名对应XML中<insert>/<delete>/<update>/<select>标签的id属性值
 * - MyBatis在运行时会通过JDK动态代理为该接口生成代理对象，方法调用时自动匹配并执行对应的SQL语句
 * - markAsRead和countUnread方法使用注解方式定义SQL，其余方法由XML映射文件提供
 * - IdleMessage表用于存储闲置交易中买卖双方之间的留言/私信对话记录
 */
public interface IdleMessageMapper {

    /**
     * 新增闲置物品留言/私信记录
     * XML中对应：<insert id="insert" parameterType="com.wll.common.entity.IdleMessage">
     * 通过IdleMessageMapper.xml中配置的INSERT SQL语句，将IdleMessage对象的所有属性值插入到idle_message表中
     * @param idleMessage 要新增的留言实体对象，包含闲置物品ID、发送者ID、接收者ID、消息内容、是否已读等属性
     * @return 受影响的行数（int类型），大于0表示插入成功，0表示插入失败
     */
    int insert(IdleMessage idleMessage);

    /**
     * 根据主键ID删除闲置物品留言记录
     * XML中对应：<delete id="deleteById" parameterType="java.lang.Integer">
     * 通过IdleMessageMapper.xml中配置的DELETE SQL语句，删除idle_message表中指定ID的记录
     * @param id 要删除的留言记录的主键ID值
     * @return 受影响的行数（int类型），大于0表示删除成功，0表示未找到对应记录
     */
    int deleteById(Integer id);

    /**
     * 根据会话条件查询闲置物品留言记录（即查询某两个用户关于某件闲置物品的所有对话）
     * XML中对应：<select id="selectByConversation" resultType="com.wll.common.entity.IdleMessage">
     * 通过IdleMessageMapper.xml中配置的SELECT SQL语句，查询idle_message表中指定会话的所有消息记录
     * @Param("idleId")注解：将方法参数idleId绑定到SQL中的#{idleId}占位符，用于指定闲置物品ID
     * @Param("userId1")注解：将方法参数userId1绑定到SQL中的#{userId1}占位符，代表会话中一方用户ID
     * @Param("userId2")注解：将方法参数userId2绑定到SQL中的#{userId2}占位符，代表会话中另一方用户ID
     * 当方法有多个参数时，必须使用@Param注解为每个参数指定名称，否则MyBatis会使用arg0/arg1/param1/param2等默认名称，可读性差且容易出错
     * SQL通常按时间顺序排列消息，用于在聊天界面中展示完整的对话历史
     * @param idleId 闲置物品的ID值，用于标识哪个闲置物品的对话
     * @param userId1 会话中一方的用户ID值
     * @param userId2 会话中另一方的用户ID值
     * @return 该会话中的所有IdleMessage消息列表（List集合），按时间排序，无结果时返回空列表
     */
    List<IdleMessage> selectByConversation(@Param("idleId") Integer idleId, @Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    /**
     * 将指定会话中的未读消息标记为已读（使用注解方式定义SQL）
     * @Update注解：声明该方法执行一条UPDATE类型的SQL更新语句
     * 注解中的SQL："update `idle_message` set is_read = 1 where idle_id = #{idleId} and sender_id = #{senderId} and receiver_id = #{receiverId}"
     * 表示将idle_message表中满足三个条件的记录的is_read字段更新为1（已读状态）
     * 三个条件：idle_id匹配（指定物品）、sender_id匹配（指定发送者）、receiver_id匹配（指定接收者）
     * #{idleId}、#{senderId}、#{receiverId}是MyBatis的参数占位符，使用PreparedStatement方式安全地设置参数值
     * @Param("idleId")将方法参数绑定到SQL中的#{idleId}
     * @Param("senderId")将方法参数绑定到SQL中的#{senderId}
     * @Param("receiverId")将方法参数绑定到SQL中的#{receiverId}
     * 该方法在用户打开聊天对话时调用，将该对话中对方发来的未读消息批量标记为已读
     * @param idleId 闲置物品的ID值
     * @param senderId 消息发送者的用户ID值（即对方）
     * @param receiverId 消息接收者的用户ID值（即当前用户）
     * @return 受影响的行数（int类型），表示被标记为已读的消息数量
     */
    @Update("update `idle_message` set is_read = 1 where idle_id = #{idleId} and sender_id = #{senderId} and receiver_id = #{receiverId}")
    int markAsRead(@Param("idleId") Integer idleId, @Param("senderId") Integer senderId, @Param("receiverId") Integer receiverId);

    /**
     * 统计指定用户的未读消息数量（使用注解方式定义SQL）
     * 注意：此处原代码使用@Update注解，但SQL是SELECT查询语句，这实际上是一个BUG
     * @Update注解：声明该方法执行一条UPDATE类型的SQL更新语句（但实际执行的SQL是SELECT COUNT查询）
     * 注解中的SQL："select count(*) from `idle_message` where receiver_id = #{receiverId} and is_read = 0"
     * 表示从idle_message表中统计receiver_id等于指定值且is_read等于0（未读）的记录总数
     * COUNT(*)是SQL的聚合函数，用于统计满足条件的记录行数
     * #{receiverId}是MyBatis的参数占位符，使用PreparedStatement方式安全地设置参数值
     * 该方法用于在消息图标上显示未读消息的红色角标数字，提醒用户有新消息
     * @param receiverId 当前用户的ID值（作为消息接收者）
     * @return 该用户的未读消息总数（int类型）
     */
    @Update("select count(*) from `idle_message` where receiver_id = #{receiverId} and is_read = 0")
    int countUnread(Integer receiverId);
}
