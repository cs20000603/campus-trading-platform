// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Hutool工具库的DateUtil类，提供日期时间工具方法（如now()获取当前时间字符串）
import cn.hutool.core.date.DateUtil;
// 导入Comment实体类，对应数据库comment（评论）表的ORM映射，包含id、商品ID(goodsId)、用户ID(userId)、评论内容(content)、评论时间(time)等字段
import com.wll.common.entity.Comment;
// 导入CommentMapper数据访问接口，封装对comment表的所有数据库CRUD操作
import com.wll.common.mapper.CommentMapper;
// 导入PageHelper分页插件类，其startPage方法会拦截紧随其后的SQL查询并自动追加LIMIT分页逻辑
import com.github.pagehelper.PageHelper;
// 导入PageInfo分页结果封装类，包含当前页数据列表、总记录数、总页数等完整分页信息
import com.github.pagehelper.PageInfo;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件，被IoC容器管理
import org.springframework.stereotype.Service;

// 导入Java标准库的List接口，用于接收数据库查询返回的列表结果集
import java.util.List;

/**
 * 评论业务处理服务
 * 负责商品评论的增删改查操作
 * 核心逻辑：新增评论时自动记录当前系统时间作为评论时间，调用方只需传入评论内容等业务字段
 * 评论功能允许用户对已购买的商品发表评价，其他用户可查看商品的所有评论
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class CommentService {

    // @Resource 注解：按照名称从Spring容器中注入CommentMapper Bean，用于执行comment表的数据库CRUD操作
    @Resource
    private CommentMapper commentMapper;

    /**
     * 新增评论
     * 自动设置评论时间为当前系统时间，调用方无需手动设置时间字段
     * @param comment 评论实体对象，必须包含 goodsId（评论哪个商品）、userId（谁评论的）、content（评论内容）等字段
     */
    public void add(Comment comment) {
        // 第一步：自动设置评论时间为当前系统时间字符串
        comment.setTime(DateUtil.now());
        // 第二步：调用Mapper层insert方法将评论记录插入到comment数据库表中
        commentMapper.insert(comment);
    }

    /**
     * 根据ID删除评论
     * @param id 评论ID（主键），对应comment表中的自增id字段
     */
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从comment表中删除对应评论记录
        commentMapper.deleteById(id);
    }

    /**
     * 更新评论内容（如修改评论文字）
     * @param comment 包含更新字段的评论实体对象，id字段定位要更新的记录
     */
    public void updateById(Comment comment) {
        // 调用Mapper层updateById方法，按主键ID更新评论记录
        commentMapper.updateById(comment);
    }

    /**
     * 根据ID查询单条评论
     * @param id 评论ID（主键）
     * @return 评论实体对象，如果ID对应的记录不存在则返回null
     */
    public Comment selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单条评论记录
        return commentMapper.selectById(id);
    }

    /**
     * 根据条件查询所有评论（通常按商品ID筛选某商品的全部评论）
     * @param comment 查询条件实体，可设置goodsId、userId等字段作为筛选条件
     * @return 评论实体列表，无匹配结果时返回空列表（非null）
     */
    public List<Comment> selectAll(Comment comment) {
        // 调用Mapper层selectAll方法，MyBatis根据comment对象中非空字段动态生成WHERE条件
        return commentMapper.selectAll(comment);
    }

    /**
     * 分页查询评论列表
     * 使用PageHelper分页插件实现物理分页
     * @param comment 查询条件对象，用于筛选评论（通常设置goodsId查某商品的评论）
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的评论数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<Comment> selectPage(Comment comment, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 将分页参数存入ThreadLocal，后续第一个SQL查询会被拦截并自动添加LIMIT
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（MyBatis拦截器会自动在此SQL末尾追加LIMIT offset, size实现物理分页）
        List<Comment> list = commentMapper.selectAll(comment);
        // 包装为PageInfo对象，自动计算并填充total、pages、pageNum等分页元数据
        return PageInfo.of(list);
    }

}
