// 声明当前类所属的包路径，com.wll.common.service包下存放所有业务逻辑服务类
package com.wll.common.service;

// 导入com.wll.common.entity包下的所有实体类（Account, User, Goods, Carousel, Shop, Category等），
// 用于创建初始化数据时构造各实体对象
import com.wll.common.entity.*;
// 导入com.wll.common.mapper包下的所有Mapper接口（UserMapper, ShopMapper等），
// 用于在初始化数据前检查数据是否已存在（查重），以及写入原始数据
import com.wll.common.mapper.*;
// 导入MinIO客户端的ListObjectsArgs类，用于构建列出对象的请求参数（桶名、是否递归等）
import io.minio.ListObjectsArgs;
// 导入MinIO客户端主类MinioClient，用于与MinIO对象存储服务器建立连接并执行操作
import io.minio.MinioClient;
// 导入MinIO的Result迭代器元素，封装每次listObjects返回的单个对象信息
import io.minio.Result;
// 导入MinIO的消息实体Item类，表示MinIO桶中的单个文件/目录对象，包含文件名(isDir/objectName)等属性
import io.minio.messages.Item;
// 导入Jakarta标准的@Resource注解，用于按名称将Spring容器中的Bean注入到当前字段
import jakarta.annotation.Resource;
// 导入Spring的@Value注解，用于从application.yml/properties配置文件中读取MinIO相关配置值
import org.springframework.beans.factory.annotation.Value;
// 导入Spring的@Service注解，标记当前类为Spring业务逻辑层组件，被IoC容器管理
import org.springframework.stereotype.Service;

// 导入Java标准库的BigDecimal类，用于精确的货币金额运算（避免浮点数精度损失）
import java.math.BigDecimal;
// 导入Java工具包，包含ArrayList、LinkedHashMap、Date、List、Map等常用工具类和接口
import java.util.*;

/**
 * 数据初始化服务
 * 用于系统首次启动时一键初始化基础演示数据，确保系统开箱即用
 * 自动检测现有数据，已有数据时智能跳过初始化，避免重复写入
 * 初始化内容包括：
 * 1. 从MinIO对象存储中扫描并收集所有图片文件URL
 * 2. 创建测试用户（2个买家：小明/小红 + 2个商家：咖啡店主/烘焙达人）
 * 3. 为商家创建对应店铺（校园咖啡屋、甜蜜烘焙坊）
 * 4. 创建22种覆盖多品类的示例商品（饮品、烘焙、服饰、数码配件、书籍教材、生活用品）
 * 5. 创建首页轮播图（从前4个商品中选取）
 * 各模块独立初始化，已有数据则自动跳过该模块
 * @Service 注解将本类注册为Spring容器中的单例Bean
 */
@Service
public class DataInitService {

    // @Resource 注解：注入MinIO客户端Bean，用于连接MinIO对象存储服务器，列出桶中的图片文件
    @Resource private MinioClient minioClient;
    // @Resource 注解：注入用户服务，调用UserService.add()创建测试用户（确保密码默认值等业务逻辑一致性）
    @Resource private UserService userService;
    // @Resource 注解：注入商品服务，调用GoodsService.add()创建示例商品（确保默认字段如浏览量初始化一致）
    @Resource private GoodsService goodsService;
    // @Resource 注解：注入轮播图服务，调用CarouselService.add()创建首页轮播图
    @Resource private CarouselService carouselService;
    // @Resource 注解：注入UserMapper，用于在初始化用户前查询用户名是否已存在（绕过Service层的异常逻辑）
    @Resource private UserMapper userMapper;
    // @Resource 注解：注入ShopMapper，用于初始化店铺时直接操作shop表
    @Resource private ShopMapper shopMapper;

    // @Value 注解：从配置文件中读取MinIO的桶名称（存放图片的bucket）
    @Value("${minio.bucket-name}")
    private String bucketName;
    // @Value 注解：从配置文件中读取MinIO服务端点URL（如 http://localhost:9000），用于拼接图片的公开访问地址
    @Value("${minio.endpoint}")
    private String minioEndpoint;

    /**
     * 执行全部数据初始化的入口方法
     * 按顺序依次执行：加载MinIO图片 -> 初始化用户 -> 初始化店铺 -> 初始化商品 -> 初始化轮播图
     * 每个步骤的结果（新增数量）存入返回Map中，方便调用方查看初始化详情
     * @return Map包含以下key-value：
     *         - "minioImages": MinIO中的图片数量
     *         - "sampleUrl": 第一张图片的完整URL（用于前端预览）
     *         - "users": 本次新增的用户数量
     *         - "shops": 本次新增的店铺数量
     *         - "goods": 本次新增的商品数量
     *         - "carousels": 本次新增的轮播图数量
     *         - "success": true表示全部初始化成功，false表示发生异常
     *         - "error": 仅当success为false时存在，包含异常的错误消息
     */
    public Map<String, Object> initAll() {
        // 使用LinkedHashMap保持put顺序（而非HashMap的无序），确保前端按序展示初始化信息
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            // 第一步：列出MinIO桶中的所有图片文件，获取可公开访问的URL列表
            List<String> minioImages = listMinioImages();
            // 记录图片数量
            result.put("minioImages", minioImages.size());
            // 如果至少有一张图片，将第一张图片URL作为示例URL供前端预览
            if (!minioImages.isEmpty()) {
                result.put("sampleUrl", minioImages.get(0));
            }
            // 第二步：初始化测试用户（买家+商家），返回新增数量
            result.put("users", initUsers());
            // 第三步：初始化商家店铺，返回新增店铺数量
            result.put("shops", initShops());
            // 第四步：初始化示例商品（传入图片URL列表用于为商品分配图片），返回新增商品数量
            result.put("goods", initGoods(minioImages));
            // 第五步：初始化首页轮播图（传入图片URL列表），返回新增轮播图数量
            result.put("carousels", initCarousels(minioImages));
            // 全部成功，标记success为true
            result.put("success", true);
        } catch (Exception e) {
            // 任一步骤异常，标记success为false并记录错误信息
            result.put("success", false);
            result.put("error", e.getMessage());
            // 打印异常堆栈，便于运维排查
            e.printStackTrace();
        }
        // 返回包含所有初始化结果的Map
        return result;
    }

    /**
     * 从MinIO对象存储中列出所有图片文件
     * 递归遍历整个桶（包括所有子目录），筛选出常见图片格式的文件，返回可公开访问的完整URL列表
     * @return 图片文件URL列表，每个URL格式为 {minioEndpoint}/{bucketName}/{objectName}
     * @throws Exception 当MinIO连接失败或读取对象列表异常时向上抛出
     */
    private List<String> listMinioImages() throws Exception {
        // 创建ArrayList用于存放收集到的图片URL
        List<String> urls = new ArrayList<>();
        // 调用MinIO客户端的listObjects方法递归遍历桶中的所有对象
        // ListObjectsArgs.builder() 构建请求参数：bucket设置桶名，recursive(true)表示递归遍历子目录
        Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder().bucket(bucketName).recursive(true).build());
        // 遍历MinIO返回的每个对象结果（Result<Item>封装了每次迭代的元素和可能的错误）
        for (Result<Item> r : results) {
            try {
                // 尝试从Result中获取Item对象（如果该对象读取失败则会抛出异常）
                Item item = r.get();
                // 筛选条件1：不是目录（目录无法作为图片URL使用）
                // 筛选条件2：文件名匹配图片扩展名（jpg/jpeg/png/gif/webp/bmp），忽略大小写
                if (!item.isDir() && item.objectName().matches(".*\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
                    // 拼接完整的可公开访问URL：端点/桶名/对象路径
                    urls.add(minioEndpoint + "/" + bucketName + "/" + item.objectName());
                }
            } catch (Exception ignored) {
                // 跳过无法读取的对象（如权限问题导致无法访问的项），继续处理下一个
            }
        }
        // 返回收集到的所有图片URL列表
        return urls;
    }

    /**
     * 初始化测试用户
     * 创建4个测试账号：2个买家（普通用户）+ 2个商家（merchant角色）
     * 每个用户的初始余额用于后续下单测试
     * 通过 userMapper.selectByUsername 检查用户名是否已存在，存在则跳过
     * @return 本次新增的用户数量（首次运行通常为4，后续运行为0）
     */
    private int initUsers() {
        // 计数器，记录本次新增的用户数量
        int count = 0;
        // 定义4个测试用户数据：用户名、密码、姓名、角色、初始余额、手机号
        String[][] users = {
            {"buyer1", "123456", "小明", "普通用户", "500.00", "13800000001"},  // 买家1，余额500元
            {"buyer2", "123456", "小红", "普通用户", "300.00", "13800000002"},  // 买家2，余额300元
            {"seller1", "123456", "咖啡店主", "merchant", "2000.00", "13800000003"},  // 商家1，余额2000元
            {"seller2", "123456", "烘焙达人", "merchant", "1500.00", "13800000004"},  // 商家2，余额1500元
        };
        // 遍历每个用户数据数组
        for (String[] d : users) {
            // 先通过用户名查询数据库，如果返回null说明该用户名不存在，可以创建
            if (userMapper.selectByUsername(d[0]) == null) {
                // 构造User实体并设置各个字段
                User u = new User();
                u.setUsername(d[0]);    // 用户名
                u.setPassword(d[1]);    // 密码
                u.setName(d[2]);        // 姓名（昵称）
                u.setRole(d[3]);        // 角色（普通用户/merchant商家）
                u.setAccount(new BigDecimal(d[4]));  // 初始余额（BigDecimal精确表示货币）
                u.setPhone(d[5]);       // 手机号
                // 通过UserService.add创建用户（会触发默认值设置和唯一性校验等业务逻辑）
                userService.add(u);
                // 新增计数+1
                count++;
            }
        }
        // 返回本次新增的用户数量
        return count;
    }

    /**
     * 初始化商家店铺
     * 为已存在的商家用户（seller1/seller2）创建对应的店铺实体
     * 只有"审核通过"状态的店铺才会在前台商城页面展示
     * 如果数据库中已有店铺数据则跳过整个初始化（避免重复创建）
     * @return 本次新增的店铺数量（首次运行通常为2，后续运行为0）
     */
    private int initShops() {
        int count = 0;
        // 查询现有店铺列表，如果已经有数据（非null且非空），直接返回0跳过初始化
        List<Shop> shops = shopMapper.selectAll(null);
        if (shops != null && !shops.isEmpty()) return 0;

        // 为seller1创建"校园咖啡屋"店铺
        User s1 = userMapper.selectByUsername("seller1");
        if (s1 != null) {
            Shop sp = new Shop();
            sp.setName("校园咖啡屋");          // 店铺名称
            sp.setDescription("现磨咖啡，新鲜直达");  // 店铺简介
            sp.setOwnerId(s1.getId());        // 关联商家用户ID
            sp.setStatus("审核通过");           // 设置状态为审核通过（商家可见可上架商品）
            sp.setPhone("13800000003");        // 店铺联系电话
            sp.setAddress("一食堂旁");          // 店铺地址
            sp.setType("饮品");                // 店铺类型（饮品店）
            sp.setCreateTime("2025-09-01");    // 开业时间
            shopMapper.insert(sp);             // 插入shop表
            count++;
        }
        // 为seller2创建"甜蜜烘焙坊"店铺
        User s2 = userMapper.selectByUsername("seller2");
        if (s2 != null) {
            Shop sp = new Shop();
            sp.setName("甜蜜烘焙坊");
            sp.setDescription("手工面包、蛋糕、甜品");
            sp.setOwnerId(s2.getId());
            sp.setStatus("审核通过");
            sp.setPhone("13800000004");
            sp.setAddress("学生活动中心");
            sp.setType("烘焙");                // 店铺类型（烘焙店）
            sp.setCreateTime("2025-09-05");
            shopMapper.insert(sp);
            count++;
        }
        return count;
    }

    /**
     * 初始化示例商品数据
     * 创建22种覆盖多品类的商品，为系统提供丰富的演示数据
     * 商品分类映射：1-3为饮品，4-6为烘焙，7-9为服饰，10为数码配件，11为书籍教材，12为生活用品
     * 饮品品类商品自动关联到"校园咖啡屋"，烘焙品类商品自动关联到"甜蜜烘焙坊"
     * 商品图片从MinIO图片列表中轮转取用（第i个商品取 images[i % images.size()]）
     * @param bucketImages MinIO桶中的图片URL列表，作为商品的展示图片来源
     * @return 本次新增的商品数量
     * @throws Exception 如果MinIO桶中没有图片，抛出RuntimeException无法初始化商品
     */
    private int initGoods(List<String> bucketImages) throws Exception {
        // 检查已有商品数量，如果>=5个则认为已初始化过，跳过
        List<Goods> existing = goodsService.selectAll(new Goods());
        if (existing != null && existing.size() >= 5) return 0;

        // 商品必须有图片，如果MinIO中没有图片则无法初始化商品
        if (bucketImages.isEmpty()) {
            throw new RuntimeException("MinIO 桶中没有图片，请先上传图片后再初始化数据");
        }

        // 查找已创建的店铺ID，用于将商品关联到对应店铺
        List<Shop> shops = shopMapper.selectAll(null);
        Integer coffeeId = null;  // 校园咖啡屋的店铺ID
        Integer bakeryId = null;  // 甜蜜烘焙坊的店铺ID
        if (shops != null) {
            for (Shop s : shops) {
                if ("校园咖啡屋".equals(s.getName())) coffeeId = s.getId();
                if ("甜蜜烘焙坊".equals(s.getName())) bakeryId = s.getId();
            }
        }

        // 定义22个示例商品的原始数据：名称、单价、描述、库存、分类ID
        // 分类ID说明：1=美式咖啡类, 2=茶饮类, 3=奶茶类, 4=面包类, 5=甜品类, 6=蛋糕类,
        //            7=配饰类, 8=服装类, 10=数码类, 11=书籍类, 12=生活用品类
        String[][] data = {
            // === 饮品系列（分类1-3）===
            {"美式咖啡",       "12.00",  "经典美式，现磨咖啡豆",              "200", "1"},
            {"拿铁咖啡",       "15.00",  "浓郁奶泡搭配意式浓缩",              "180", "1"},
            {"卡布奇诺",       "16.00",  "奶泡绵密，口感醇厚",                "150", "1"},
            {"柠檬绿茶",       "10.00",  "清爽柠檬搭配绿茶",                  "300", "2"},
            {"满杯百香果",     "14.00",  "新鲜百香果，酸甜可口",              "250", "2"},
            {"蜜桃乌龙茶",     "13.00",  "蜜桃果肉+乌龙茶底",                 "200", "2"},
            {"珍珠奶茶",       "12.00",  "Q弹珍珠，经典味道",                 "350", "3"},
            {"椰果奶茶",       "12.00",  "椰果粒粒分明，清爽香甜",            "280", "3"},
            // === 烘焙系列（分类4-6）===
            {"全麦吐司",       "8.00",   "健康全麦，早餐首选",                "150", "4"},
            {"提拉米苏",       "18.00",  "经典意式甜品，入口即化",            "80",  "5"},
            {"芒果慕斯",       "16.00",  "新鲜芒果，轻盈慕斯",                "90",  "5"},
            {"草莓奶油蛋糕",   "88.00",  "6寸草莓奶油蛋糕，新鲜现做",         "30",  "6"},
            // === 服饰系列（分类7-9）===
            {"简约帆布包",     "29.00",  "文艺简约帆布单肩包",                "100", "7"},
            {"学院风百褶裙",   "59.00",  "韩版高腰A字百褶裙",                "60",  "8"},
            {"连帽卫衣",       "69.00",  "宽松纯色加绒卫衣",                  "80",  "8"},
            // === 数码配件系列（分类10）===
            {"Type-C数据线",   "12.00",  "1米快充数据线，编织材质",           "300", "10"},
            {"无线蓝牙耳机",   "79.00",  "蓝牙5.3，续航8小时",                "100", "10"},
            {"手机支架",       "9.90",   "可折叠桌面手机支架",                "200", "10"},
            // === 书籍教材系列（分类11）===
            {"高等数学第七版", "25.00",  "同济大学数学系，九成新",            "50",  "11"},
            {"英语四级词汇",   "15.00",  "星火英语四级词汇书",                "120", "11"},
            // === 生活用品系列（分类12）===
            {"保温杯",         "35.00",  "316不锈钢，500ml",                 "150", "12"},
            {"桌面收纳盒",     "19.90",  "三层抽屉式桌面收纳",                "200", "12"},
        };

        int count = 0;
        // 遍历所有商品数据数组，逐一创建Goods实体并插入数据库
        for (int i = 0; i < data.length; i++) {
            String[] d = data[i];
            Goods g = new Goods();
            g.setName(d[0]);  // 商品名称
            // 图片轮转分配：第i个商品使用第 i%图片总数 张图片（确保所有图片都能被利用，循环使用）
            g.setImg(bucketImages.get(i % bucketImages.size()));
            g.setPrice(new BigDecimal(d[1]));  // 商品单价（BigDecimal保证金额精度）
            g.setDescription(d[2]);            // 商品描述
            g.setStore(Integer.parseInt(d[3])); // 库存数量
            g.setCategoryId(Integer.parseInt(d[4])); // 分类ID
            g.setStatus("上架");               // 状态设为上架（在前台可见可购买）
            int cat = Integer.parseInt(d[4]); // 解析分类ID为整数（用于判断关联店铺）
            // 分类1-3（饮品系列）→ 关联到校园咖啡屋
            if (cat <= 3) g.setShopId(coffeeId);
            // 分类4-6（烘焙系列）→ 关联到甜蜜烘焙坊
            else if (cat <= 6) g.setShopId(bakeryId);
            // 其他分类不关联店铺（通用商品）
            // 调用GoodsService.add创建商品（会自动设置浏览量=0、销量=0、上架时间等默认字段）
            goodsService.add(g);
            count++;
        }
        return count;
    }

    /**
     * 初始化首页轮播图
     * 从已有商品中选取前4个作为轮播图展示素材，图片从MinIO图片列表轮转取用
     * 如果数据库中已有轮播图记录，则跳过整个初始化
     * @param bucketImages MinIO桶中的图片URL列表
     * @return 本次新增的轮播图数量（最多4个）
     * @throws Exception 如果MinIO桶中没有图片，抛出RuntimeException
     */
    private int initCarousels(List<String> bucketImages) throws Exception {
        // 检查是否已有轮播图数据，有则跳过
        List<Carousel> existing = carouselService.selectAll(null);
        if (existing != null && !existing.isEmpty()) return 0;

        // 轮播图必须有图片
        if (bucketImages.isEmpty()) {
            throw new RuntimeException("MinIO 桶中没有图片，请先上传图片后再初始化数据");
        }

        // 获取所有商品列表（用于关联轮播图到商品）
        List<Goods> allGoods = goodsService.selectAll(new Goods());
        int count = 0;
        // 创建轮播图，最多4个（取min(4, 商品总数)）
        for (int i = 0; i < 4 && i < allGoods.size(); i++) {
            Carousel c = new Carousel();
            // 轮播图图片从MinIO列表中轮转取用
            c.setImg(bucketImages.get(i % bucketImages.size()));
            // 关联商品ID（点击轮播图可跳转到对应商品详情页）
            c.setGoodsId(allGoods.get(i).getId());
            // 调用CarouselService.add将轮播图插入数据库
            carouselService.add(c);
            count++;
        }
        return count;
    }
}
