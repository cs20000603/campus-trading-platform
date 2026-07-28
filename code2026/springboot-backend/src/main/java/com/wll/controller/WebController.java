// 声明该类所属的包路径，com.wll.controller包存放所有Spring MVC控制器类
package com.wll.controller;

// Hutool日期时间工具类-DateField：日期字段枚举（如DAY_OF_YEAR表示按天），用于日期范围计算
import cn.hutool.core.date.DateField;
// Hutool日期时间工具类-DateTime：增强的日期时间对象，继承自java.util.Date，支持链式日期运算
import cn.hutool.core.date.DateTime;
// Hutool日期时间工具类-DateUtil：日期工具类，提供日期格式化、日期偏移、日期范围生成等静态方法
import cn.hutool.core.date.DateUtil;
// Hutool字符串工具类-StrUtil：字符串工具类，提供isBlank、isEmpty等字符串判空静态方法
import cn.hutool.core.util.StrUtil;
// 导入统一响应结果类Result，用于封装API返回数据（成功/失败状态码、消息、数据）
import com.wll.common.dto.Result;
// 通配符导入：导入common.entity包下所有实体类（Account、Admin、Category、Goods、OrderDetail、Orders、Shop、User等）
import com.wll.common.entity.*;
// 导入订单详情数据访问接口OrderDetailMapper，MyBatis映射器，提供订单详情表的数据库操作
import com.wll.common.mapper.OrderDetailMapper;
// 导入店铺数据访问接口ShopMapper，MyBatis映射器，提供店铺表的数据库CRUD操作
import com.wll.common.mapper.ShopMapper;
// 导入用户数据访问接口UserMapper，MyBatis映射器，提供用户表的数据库CRUD操作
import com.wll.common.mapper.UserMapper;
// 通配符导入：导入common.service包下所有服务接口（AdminService、UserService、OrdersService、GoodsService等）
import com.wll.common.service.*;
// 导入WebSocket事件类型枚举WebSocketEventType，定义WebSocket推送事件类型（如SHOP_APPLY、SHOP_APPROVE等）
import com.wll.common.websocket.WebSocketEventType;
// 导入WebSocket消息体类WebSocketMessage，封装WebSocket推送消息的数据结构（事件类型、目标用户ID、消息内容）
import com.wll.common.websocket.WebSocketMessage;
// 导入WebSocket推送服务WebSocketPushService，提供向指定用户或Topic推送WebSocket消息的功能
import com.wll.common.websocket.WebSocketPushService;
// 导入Jakarta资源注入注解@Resource，用于按名称/类型自动注入Spring管理的Bean
import jakarta.annotation.Resource;
// 导入Spring MVC相关注解：@RestController、@GetMapping、@PostMapping、@PutMapping、@RequestBody、@RequestParam等
import org.springframework.web.bind.annotation.*;

// 导入Java的大数BigDecimal类，用于精确的货币金额计算（避免float/double浮点数精度丢失）
import java.math.BigDecimal;
// 导入Java集合框架工具类（ArrayList、HashMap、Date等）
import java.util.*;


/**
 * 前端公共控制器（Web入口）
 * 处理系统核心业务接口，包括：用户登录/注册/密码管理、店铺申请与管理、
 * 数据统计（总览、折线图、饼图）等功能
 * 该控制器映射到根路径，无统一请求前缀
 */
// @RestController注解：标记该类为RESTful控制器，返回值自动序列化为JSON
@RestController
// 无@RequestMapping：该控制器直接映射到根路径"/"，接口路径见各方法上的映射注解
public class WebController {

    // @Resource注解：注入AdminService，用于管理员登录验证和密码修改
    @Resource
    private AdminService adminService;
    // @Resource注解：注入UserService，用于普通用户的登录、注册、密码重置、查询等业务操作
    @Resource
    private UserService userService;
    // @Resource注解：注入OrdersService，用于订单查询（数据统计时获取所有订单）
    @Resource
    private OrdersService ordersService;
    // @Resource注解：注入GoodsService，用于商品查询（数据统计时获取商品总数）
    @Resource
    private GoodsService goodsService;
    // @Resource注解：注入CategoryService，用于分类查询（饼图统计时遍历各分类营业额）
    @Resource
    private CategoryService categoryService;
    // @Resource注解：注入OrderDetailMapper，用于查询订单详情（饼图统计时关联订单和商品分类）
    @Resource
    private OrderDetailMapper orderDetailMapper;
    // @Resource注解：注入CaptchaService，用于管理员登录时验证Redis中的验证码
    @Resource
    private CaptchaService captchaService;
    // @Resource注解：注入ShopMapper，用于店铺的数据库操作（用户申请开店、查询店铺）
    @Resource
    private ShopMapper shopMapper;
    // @Resource注解：注入UserMapper，用于直接操作用户表数据
    @Resource
    private UserMapper userMapper;
    // @Resource注解：注入WebSocketPushService，用于向管理员推送新店铺申请等实时通知
    @Resource
    private WebSocketPushService wsPushService;


    /**
     * 默认根路径请求接口
     * 请求方式：GET /
     * 用于检查服务是否正常运行（健康检查接口）
     * @return Result 空成功响应（表示服务可用）
     */
    // @GetMapping注解：将HTTP GET请求映射到该方法，路径为"/"
    @GetMapping("/")
    // hello方法：服务健康检查，返回空成功表示服务正常运行
    public Result hello() {
        // 直接返回成功响应，无具体数据
        return Result.success();
    }

    /**
     * 普通用户登录（账号+密码）
     * 请求方式：POST /login/user
     * 验证码由前端负责校验，此接口仅验证用户名和密码
     * @param account 包含username和password字段的JSON请求体（Account实体，映射到请求体JSON）
     * @return Result 登录成功时返回包含用户信息的Account对象，失败时返回错误提示
     */
    // @PostMapping注解：HTTP POST映射，路径为/login/user
    @PostMapping("/login/user")
    // loginUser方法：普通用户通过用户名和密码登录
    // @RequestBody注解：将HTTP请求体JSON反序列化为Account对象
    public Result loginUser(@RequestBody Account account) {
        // 校验账号（用户名）不能为空
        if (account.getUsername() == null || account.getPassword() == null) {
            // 用户名或密码为空时返回错误
            return Result.error("账号和密码不能为空");
        }
        // 调用用户服务进行登录验证：检查用户名是否存在、密码是否匹配
        Account ac = userService.login(account.getUsername(), account.getPassword());
        if (ac == null) {
            // 登录失败（用户名或密码错误），返回错误提示
            return Result.error("登录失败，账号或密码错误");
        }
        // 登录成功，返回包含用户信息的Account对象（含token、userId、角色等）
        return Result.success(ac);
    }

    /**
     * 管理员登录（用户名+密码+Redis验证码）
     * 请求方式：POST /login/admin
     * 需要先通过 /captcha/admin 接口获取验证码，验证码存储在Redis中
     * @param account 包含username、password和captcha字段的JSON请求体
     * @return Result 登录成功返回管理员信息，失败返回具体错误提示
     */
    // @PostMapping注解：HTTP POST映射，路径为/login/admin
    @PostMapping("/login/admin")
    // loginAdmin方法：管理员通过用户名、密码和验证码登录
    public Result loginAdmin(@RequestBody Account account) {
        // 校验用户名和密码不能为空
        if (account.getUsername() == null || account.getPassword() == null) {
            return Result.error("用户名和密码不能为空");
        }
        // 校验验证码不能为空
        if (account.getCaptcha() == null) {
            return Result.error("验证码不能为空");
        }

        // 第一步：验证Redis中存储的验证码是否正确（通过captchaService对比Redis中的值）
        boolean captchaValid = captchaService.verifyAdminCaptcha(account.getUsername(), account.getCaptcha());
        if (!captchaValid) {
            // 验证码错误或已过期（Redis中验证码有过期时间），提示重新获取
            return Result.error("验证码错误或已过期，请重新获取");
        }

        // 第二步：验证用户名和密码是否正确
        Account ac = adminService.login(account);
        if (ac == null) {
            // 用户名或密码错误
            return Result.error("登录失败，用户名或密码错误");
        }
        // 管理员登录成功，返回管理员信息
        return Result.success(ac);
    }

    /**
     * 用户注册
     * 请求方式：POST /register
     * 校验两次输入的密码是否一致，然后创建新用户
     * @param user 用户实体对象（JSON请求体），包含用户名、密码(password)、确认密码(newPassword)等信息
     * @return Result 注册成功响应，或包含错误信息的失败响应
     */
    // @PostMapping注解：HTTP POST映射，路径为/register
    @PostMapping("/register")
    // register方法：新用户注册
    public Result register(@RequestBody User user) {
        try {
            // 校验两次输入的密码是否一致：password为密码字段，newPassword为确认密码字段
            if (!user.getPassword().equals(user.getNewPassword())) {
                // 两次密码不一致，拒绝注册
                return Result.error("两次输入密码不一致");
            }
            // 调用用户服务添加新用户到数据库（包含密码加密等处理）
            userService.add(user);
            // 注册成功
            return Result.success();
        } catch (Exception e) {
            // 打印异常堆栈以便调试
            e.printStackTrace();
            // 返回注册失败的错误信息（如"用户名已存在"等数据库约束冲突）
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 修改密码
     * 请求方式：PUT /updatePassword
     * 根据角色（管理员/普通用户）分别调用对应的服务修改密码
     * @param account 包含role(角色:"管理员"/"普通用户")、username(用户名)、password(新密码)的JSON请求体
     * @return Result 操作成功响应
     */
    // @PutMapping注解：HTTP PUT映射，路径为/updatePassword
    @PutMapping("/updatePassword")
    // updatePassword方法：根据用户角色修改密码
    public Result updatePassword(@RequestBody Account account) {
        // 根据角色字段的值判断是管理员还是普通用户，调用对应服务
        if ("管理员".equals(account.getRole())) {
            // 管理员修改密码：调用adminService更新管理员表中的密码字段
            adminService.updatePassword(account);
        }
        if ("普通用户".equals(account.getRole())) {
            // 普通用户修改密码：调用userService更新用户表中的密码字段
            userService.updatePassword(account);
        }
        // 返回成功响应
        return Result.success();
    }

    /**
     * 忘记密码 - 通过用户名重置密码
     * 请求方式：POST /resetPassword
     * 无需登录即可重置，新密码至少6位
     * @param params JSON请求体，包含username(用户名)和newPassword(新密码)字段
     * @return Result 操作成功或错误提示（用户名或密码为空、密码长度不够）
     */
    // @PostMapping注解：HTTP POST映射，路径为/resetPassword
    @PostMapping("/resetPassword")
    // resetPassword方法：通过用户名直接重置密码（忘记密码场景）
    public Result resetPassword(@RequestBody Map<String, String> params) {
        // 从请求体Map中提取用户名
        String username = params.get("username");
        // 从请求体Map中提取新密码
        String newPassword = params.get("newPassword");
        // 使用Hutool的StrUtil.isBlank校验用户名和新密码不能为空或全空格
        if (StrUtil.isBlank(username) || StrUtil.isBlank(newPassword)) {
            return Result.error("用户名和新密码不能为空");
        }
        // 校验新密码长度至少6位
        if (newPassword.length() < 6) {
            return Result.error("新密码至少6位");
        }
        // 调用用户服务重置密码：根据用户名查找用户并更新密码
        userService.resetPassword(username, newPassword);
        // 返回成功响应
        return Result.success();
    }

    // ===== 店铺相关接口 =====

    /**
     * 申请开店
     * 请求方式：POST /userShop/register
     * 每个用户只能申请一个店铺，申请后状态为"线上审核中"
     * 申请成功后会通过WebSocket通知管理员
     * @param shop 店铺实体对象（JSON请求体），包含店铺名称、店主ID等信息
     * @return Result 操作成功或错误提示（名称/用户为空、已申请过店铺）
     */
    // @PostMapping注解：HTTP POST映射，路径为/userShop/register
    @PostMapping("/userShop/register")
    // shopRegister方法：普通用户申请开店
    public Result shopRegister(@RequestBody Shop shop) {
        // 校验店铺名称不能为空（使用Hutool工具类判空）
        if (StrUtil.isBlank(shop.getName())) {
            return Result.error("店铺名称不能为空");
        }
        // 校验店主ID不能为空
        if (shop.getOwnerId() == null) {
            return Result.error("用户信息缺失");
        }
        // 检查该用户是否已经申请过店铺（一个用户只能开一个店）
        Shop existing = shopMapper.selectByOwnerId(shop.getOwnerId());
        if (existing != null) {
            // 已存在该用户的店铺记录，不允许重复申请
            return Result.error("您已经申请过店铺");
        }
        // 设置店铺初始状态为"线上审核中"，需要管理员审核通过才能营业
        shop.setStatus("线上审核中");
        // 设置店铺创建时间为当前时间（使用Hutool的DateUtil.now()获取当前日期时间字符串）
        shop.setCreateTime(DateUtil.now());
        // 插入店铺数据到数据库
        shopMapper.insert(shop);
        // 通过WebSocket向管理员推送新店铺申请通知
        wsPushService.pushToTopic("/topic/admin",
            new WebSocketMessage(WebSocketEventType.SHOP_APPLY, null,
                "有新店铺申请：「" + shop.getName() + "」正在等待审核"));
        // 返回成功响应
        return Result.success();
    }

    /**
     * 查询我的店铺
     * 请求方式：GET /userShop/my?userId=xxx
     * 根据用户ID查询该用户拥有的店铺信息
     * @param userId 用户ID（URL查询参数，必填）
     * @return Result 包含Shop对象的成功响应（可能为null表示未开店）
     */
    // @GetMapping注解：HTTP GET映射，路径为/userShop/my
    @GetMapping("/userShop/my")
    // shopMy方法：根据用户ID查询其拥有的店铺
    public Result shopMy(@RequestParam Integer userId) {
        // 通过ShopMapper根据店主ID查询店铺记录（每个用户最多一个店）
        Shop shop = shopMapper.selectByOwnerId(userId);
        // 返回店铺数据（可能为null，表示该用户还未开店）
        return Result.success(shop);
    }

    /**
     * 更新店铺信息
     * 请求方式：PUT /userShop/update
     * 仅店主本人可操作。如果店铺之前被审核拒绝，重新提交时会自动重置为"线上审核中"
     * @param shop 店铺实体对象（JSON请求体），包含要修改的字段和主键ID
     * @return Result 操作成功或错误提示（店铺不存在/无权操作）
     */
    // @PutMapping注解：HTTP PUT映射，路径为/userShop/update
    @PutMapping("/userShop/update")
    // shopUpdate方法：店主更新自己的店铺信息
    public Result shopUpdate(@RequestBody Shop shop) {
        // 从数据库查询原有的店铺记录
        Shop dbShop = shopMapper.selectById(shop.getId());
        if (dbShop == null) {
            // 店铺ID不存在
            return Result.error("店铺不存在");
        }
        // 验证请求中的店主ID与数据库中的店主ID是否一致（防止越权修改他人店铺）
        if (!dbShop.getOwnerId().equals(shop.getOwnerId())) {
            return Result.error("无权操作");
        }
        // 如果店铺之前被审核拒绝，重新提交修改后自动重置为"线上审核中"并清除之前的拒绝理由
        if ("审核拒绝".equals(dbShop.getStatus())) {
            shop.setStatus("线上审核中");
            shop.setRejectReason(null);
        }
        // 执行数据库更新操作
        shopMapper.updateById(shop);
        // 返回成功响应
        return Result.success();
    }

    /**
     * 数据统计总览接口
     * 请求方式：GET /count
     * 统计系统的核心运营指标：总营业额、今日营业额、商品总数、用户总数
     * 已取消的订单不计入营业额统计
     * @return Result 包含total（总营业额）、today（今日营业额）、goods（商品数）、user（用户数）的Map
     */
    // @GetMapping注解：HTTP GET映射，路径为/count
    @GetMapping("/count")
    // count方法：统计系统首页数据总览的核心运营指标
    public Result count() {
        // 查询所有订单，通过Stream过滤掉状态为"已取消"的订单（取消的订单不计入营业额）
        // toList()是Java 16+的便捷方法，将Stream收集为不可变List
        List<Orders> ordersList = ordersService.selectAll(null).stream().filter(orders -> !orders.getStatus().equals("已取消")).toList();
        // 计算总营业额：对所有有效订单的total金额求和，使用BigDecimal避免浮点数精度丢失
        // reduce(BigDecimal::add)对流中元素逐一累加，orElse(BigDecimal.ZERO)处理空流情况
        BigDecimal total = ordersList.stream().map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        // 获取今天的日期字符串（如：2025-12-05），用于筛选今日订单
        String todayDate = DateUtil.today();
        // 计算今日营业额：筛选订单时间包含今天日期的订单，对其金额求和
        BigDecimal today = ordersList.stream().filter(orders -> orders.getTime().contains(todayDate))
                .map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        // 统计系统中的商品总数：查询所有商品列表后取size
        Integer goods = goodsService.selectAll(null).size();
        // 统计系统中的用户总数：查询所有用户列表后取size
        Integer user = userService.selectByAll(null).size();
        // 组装返回的Map数据（前端仪表盘展示用）
        Map<String, Object> map = new HashMap<>();
        map.put("total", total);    // 总营业额（BigDecimal，前端显示为金额格式）
        map.put("today", today);    // 今日营业额（BigDecimal）
        map.put("goods", goods);    // 商品总数（Integer）
        map.put("user", user);      // 用户总数（Integer）
        // 返回统计数据
        return Result.success(map);
    }

    /**
     * 近7天营业额折线图数据接口
     * 请求方式：GET /selectLine
     * 统计最近7天每天的营业额，用于前端ECharts折线图展示
     * @return Result 包含date（日期数组MM-dd格式）和count（每日营业额数组）的Map
     */
    // @GetMapping注解：HTTP GET映射，路径为/selectLine
    @GetMapping("/selectLine")
    // selectLine方法：生成近7天营业额趋势数据，供前端ECharts折线图使用
    public Result selectLine() {
        // 获取当前日期
        Date date = new Date();
        // 计算起始日期：当前日期向前偏移6天（一共7天：今天 + 前6天）
        DateTime start = DateUtil.offsetDay(date, -6);
        // 生成从起始日到今天的日期范围列表（按天枚举，每个元素是一个DateTime对象）
        List<DateTime> dateTimes = DateUtil.rangeToList(start, date, DateField.DAY_OF_YEAR);
        // 将日期列表转换为MM-dd格式字符串列表（如["06-26", "06-27", ..., "07-01"]），并排序
        List<String> dateStrList = dateTimes.stream().map(dateTime -> DateUtil.format(dateTime, "MM-dd")).sorted().toList();
        // 获取所有有效订单（排除已取消的）
        List<Orders> ordersList = ordersService.selectAll(null).stream().filter(orders -> !orders.getStatus().equals("已取消")).toList();
        // 获取当前年份，用于年度过滤（如2026），防止跨年数据混淆
        int year = DateUtil.year(date);
        // 创建用于存储每日营业额的列表
        ArrayList<BigDecimal> countList = new ArrayList<>();
        // 遍历每天的日期标签，计算当天的营业额总和
        for (String day : dateStrList) {
            // 筛选订单时间同时包含年份和日期（MM-dd）的订单，累加total金额
            BigDecimal total = ordersList.stream().filter(o -> o.getTime().contains(String.valueOf(year)) && o.getTime().contains(day))
                    .map(Orders::getTotal).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            // 将当日营业额添加到列表
            countList.add(total);
        }
        // 组装折线图数据：date为X轴标签数组，count为Y轴数据数组
        Map<String, Object> map = new HashMap<>();
        map.put("date", dateStrList);    // X轴：["06-26", "06-27", ..., "07-01"]格式的日期标签
        map.put("count", countList);     // Y轴：[100.50, 200.00, ...]格式的每日营业额
        // 返回折线图数据
        return Result.success(map);
    }

    /**
     * 各分类营业额饼图数据接口
     * 请求方式：GET /selectPie
     * 按商品分类统计营业额分布，用于前端ECharts饼图展示
     * 只返回营业额大于0的分类
     * @return Result 包含各分类名称和对应营业额的Map列表，如[{name:"电子产品", value:5000},{name:"书籍", value:2000}]
     */
    // @GetMapping注解：HTTP GET映射，路径为/selectPie
    @GetMapping("/selectPie")
    // selectPie方法：按分类统计营业额分布，生成饼图数据
    public Result selectPie() {
        // 创建结果列表：每个元素是一个包含name（分类名）和value（营业额）的Map
        List<Map<String, Object>> list = new ArrayList<>();
        // 获取所有商品分类
        List<Category> categoryList = categoryService.selectAll(null);
        // 声明结果Map变量，在循环中复用
        Map<String, Object> map;
        // 遍历每个商品分类，统计该分类下的总营业额
        for (Category category : categoryList) {
            // 为每个分类创建一个新的Map对象
            map = new HashMap<>();
            // 设置分类名称（如"电子产品"、"生活用品"等）
            map.put("name", category.getName());
            // 初始化该分类的营业额累计值为0
            BigDecimal total = BigDecimal.ZERO;
            // 获取所有订单详情记录，用于关联订单和商品
            List<OrderDetail> orderDetailList = orderDetailMapper.selectAll(null);
            // 遍历每条订单详情
            for (OrderDetail orderDetail : orderDetailList) {
                // 通过订单详情中的orderId查询对应的订单信息
                Integer orderId = orderDetail.getOrderId();
                Orders orders = ordersService.selectById(orderId);
                // 排除已取消的订单（已取消的订单不计入营业额）
                if (!orders.getStatus().equals("已取消")) {
                    // 通过订单详情中的goodsId查询商品信息
                    Integer goodsId = orderDetail.getGoodsId();
                    Goods goods = goodsService.selectById(goodsId);
                    // 判断该商品是否属于当前遍历的分类（通过categoryId匹配）
                    if (goods.getCategoryId().equals(category.getId())) {
                        // 如果属于当前分类，将该订单的总金额累加到分类营业额中
                        total = total.add(orders.getTotal());
                    }
                }
            }
            // 设置该分类的营业额
            map.put("value", total);
            // 只添加营业额大于0的分类到结果列表中（营业额为0的分类不显示在饼图上）
            // compareTo(BigDecimal.ZERO) > 0 表示大于0
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                list.add(map);
            }
        }
        // 返回饼图数据列表
        return Result.success(list);
    }

}
