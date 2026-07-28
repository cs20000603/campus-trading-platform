// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入Category实体类，对应数据库category（分类）表的ORM映射，包含分类id、名称(name)、店铺类型(shopType)等字段
import com.wll.common.entity.Category;
// 导入CategoryMapper数据访问接口，封装对category表的所有数据库CRUD操作及自定义查询（如selectByShopType）
import com.wll.common.mapper.CategoryMapper;
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
 * 商品分类业务处理服务
 * 负责商品分类的增删改查操作
 * 商品分类用于对商品进行归类管理（如饮品、烘焙、服饰等），不同店铺类型对应不同的分类列表
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class CategoryService {

    // @Resource 注解：按照名称从Spring容器中注入CategoryMapper Bean，用于执行category表的数据库CRUD操作
    @Resource
    private CategoryMapper categoryMapper;

    /**
     * 新增商品分类
     * @param category 分类实体对象，包含分类名称(name)、店铺类型(shopType)等字段
     */
    public void add(Category category) {
        // 调用Mapper层insert方法将分类实体插入到category数据库表中
        categoryMapper.insert(category);
    }

    /**
     * 根据ID删除商品分类
     * @param id 分类ID（主键），对应category表中的自增id字段
     */
    public void deleteById(Integer id) {
        // 调用Mapper层deleteById方法，按主键ID从category表中删除对应分类记录
        categoryMapper.deleteById(id);
    }

    /**
     * 更新分类信息（如修改分类名称）
     * @param category 包含更新字段的分类实体对象，id字段定位记录
     */
    public void updateById(Category category) {
        // 调用Mapper层updateById方法，按主键ID更新分类记录
        categoryMapper.updateById(category);
    }

    /**
     * 根据ID查询单个分类
     * @param id 分类ID（主键）
     * @return 分类实体对象，如果ID对应的记录不存在则返回null
     */
    public Category selectById(Integer id) {
        // 调用Mapper层selectById方法，按主键ID查询并返回单个分类记录
        return categoryMapper.selectById(id);
    }

    /**
     * 根据条件查询所有分类列表
     * @param category 查询条件实体，支持按字段组合筛选；传空对象则查询全部分类
     * @return 分类实体列表，无匹配结果时返回空列表
     */
    public List<Category> selectAll(Category category) {
        // 调用Mapper层selectAll方法，MyBatis根据category对象中非空字段动态生成WHERE条件
        return categoryMapper.selectAll(category);
    }

    /**
     * 根据店铺类型查询分类列表
     * 用于不同店铺类型展示不同的商品分类（如"饮品"店铺展示饮品分类，"烘焙"店铺展示烘焙分类）
     * @param shopType 店铺类型字符串（如"饮品"、"烘焙"、"服饰"等）
     * @return 该店铺类型下的所有分类列表，无匹配结果时返回空列表
     */
    public List<Category> selectByShopType(String shopType) {
        // 调用Mapper层自定义方法selectByShopType，按店铺类型筛选分类
        return categoryMapper.selectByShopType(shopType);
    }

    /**
     * 分页查询分类列表
     * 使用PageHelper分页插件实现物理分页
     * @param category 查询条件对象，用于筛选分类
     * @param pageNum 当前页码（从1开始）
     * @param pageSize 每页显示的记录条数
     * @return PageInfo分页结果对象：
     *         - getList(): 当前页的分类数据列表
     *         - getTotal(): 符合查询条件的总记录数
     *         - getPages(): 总页数
     */
    public PageInfo<Category> selectPage(Category category, Integer pageNum, Integer pageSize) {
        // PageHelper.startPage 设置分页参数到ThreadLocal，MyBatis拦截器会在后续SQL中自动追加LIMIT分页
        PageHelper.startPage(pageNum, pageSize);
        // 执行查询（此查询被PageHelper拦截，自动添加LIMIT offset, size，实现物理分页）
        List<Category> list = categoryMapper.selectAll(category);
        // 将查询结果列表包装为PageInfo对象，自动计算total、pages等分页元数据
        return PageInfo.of(list);
    }

}
