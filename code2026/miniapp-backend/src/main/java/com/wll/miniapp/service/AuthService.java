// 声明当前类所属的包路径，用于组织和管理Java类
package com.wll.miniapp.service;

// 导入Hutool随机数工具类，用于生成随机字符串（如微信用户默认用户名）
import cn.hutool.core.util.RandomUtil;
// 导入Hutool字符串工具类，用于字符串空值判断等操作
import cn.hutool.core.util.StrUtil;
// 导入Hutool的JSONObject类，用于解析微信API返回的JSON响应数据
import cn.hutool.json.JSONObject;
// 导入用户实体类，映射数据库中的用户表
import com.wll.common.entity.User;
// 导入自定义异常类，用于业务逻辑中的异常抛出（如账号不存在、密码错误等）
import com.wll.common.exception.CustomException;
// 导入用户数据访问接口，提供用户表的CRUD操作
import com.wll.common.mapper.UserMapper;
// 导入JWT工具类，用于生成和验证JWT令牌
import com.wll.miniapp.utils.JwtUtils;
// 导入微信工具类，用于调用微信小程序API（如code2session接口）
import com.wll.miniapp.utils.WechatUtils;
// 导入Spring的Service注解，标记该类为业务逻辑层组件
import org.springframework.stereotype.Service;

// 导入BigDecimal类，用于精确的金额计算（用户账户余额初始化为0）
import java.math.BigDecimal;
// 导入HashMap类，用于构建返回给前端的键值对数据
import java.util.HashMap;
// 导入Map接口，用于定义返回数据的类型
import java.util.Map;

// 标记该类为Spring Service层组件，被Spring容器管理
@Service
// 认证业务服务类，处理微信登录、账号登录、手机登录、注册、绑定手机等核心认证逻辑
public class AuthService {

    // 微信工具类实例，用于与微信服务器进行API交互（通过构造器注入）
    private final WechatUtils wechatUtils;
    // 用户数据访问接口实例，用于操作数据库中的用户表（通过构造器注入）
    private final UserMapper userMapper;
    // JWT工具类实例，用于生成和解析JWT令牌（通过构造器注入）
    private final JwtUtils jwtUtils;

    // 构造器注入依赖，Spring会自动注入所有构造器参数对应的Bean
    public AuthService(WechatUtils wechatUtils, UserMapper userMapper, JwtUtils jwtUtils) {
        // 保存注入的微信工具类实例
        this.wechatUtils = wechatUtils;
        // 保存注入的用户数据访问实例
        this.userMapper = userMapper;
        // 保存注入的JWT工具类实例
        this.jwtUtils = jwtUtils;
    }

    // 微信小程序登录方法，根据前端传来的临时授权码code完成登录流程
    public Map<String, Object> wxLogin(String code) {
        // 调用微信工具类的code2session方法，用code换取用户的openid和session_key
        JSONObject result = wechatUtils.code2session(code);
        // 检查微信API返回是否包含错误码，且错误码不为0表示调用失败
        if (result.containsKey("errcode") && result.getInt("errcode") != 0) {
            // 微信API调用失败，抛出运行时异常并附带错误信息
            throw new RuntimeException("微信登录失败: " + result.getStr("errmsg"));
        }

        // 从微信返回的JSON中提取用户的唯一标识openid
        String openid = result.getStr("openid");
        // 根据openid查询数据库中是否已存在该微信用户
        User user = userMapper.selectByOpenid(openid);
        // 标记是否需要绑定手机号（微信新用户或未绑定手机号的用户需要绑定）
        boolean needBindPhone = false;

        // 判断该微信用户是否为首次登录（数据库中不存在记录）
        if (user == null) {
            // 创建新的用户实体对象
            user = new User();
            // 生成默认用户名：wx_ + 8位随机字符串
            user.setUsername("wx_" + RandomUtil.randomString(8));
            // 微信注册用户初始密码为空（需要通过绑定手机设置密码）
            user.setPassword("");
            // 设置默认昵称为"微信用户"
            user.setName("微信用户");
            // 设置用户角色为"普通用户"
            user.setRole("普通用户");
            // 初始化账户余额为0
            user.setAccount(BigDecimal.ZERO);
            // 设置默认头像为空字符串
            user.setAvatar("");
            // 将微信的openid关联到用户记录
            user.setOpenid(openid);
            // 将新用户信息插入数据库
            userMapper.insert(user);
            // 重新查询刚插入的用户，以获取数据库自动生成的用户ID
            user = userMapper.selectByOpenid(openid);
            // 新用户需要绑定手机号
            needBindPhone = true;
        } else if (StrUtil.isBlank(user.getPhone())) {
            // 如果用户已存在但手机号为空（之前微信登录过但未绑定手机号）
            // 标记需要绑定手机号
            needBindPhone = true;
        }

        // 生成JWT令牌，包含用户ID和openid作为载荷
        String token = jwtUtils.generateToken(user.getId(), openid);
        // 将生成的token设置到用户实体中（会持久化到数据库，方便后续验证）
        user.setToken(token);
        // 更新用户信息到数据库（保存最新的token）
        userMapper.updateById(user);

        // 创建返回数据的Map容器
        Map<String, Object> data = new HashMap<>();
        // 将用户信息放入返回数据中
        data.put("user", user);
        // 将JWT令牌放入返回数据中
        data.put("token", token);
        // 将是否需要绑定手机号的标志放入返回数据中
        data.put("needBindPhone", needBindPhone);
        // 返回包含用户信息、token和绑定标志的数据
        return data;
    }

    // 通用账号密码登录方法，支持用户名或手机号作为账号登录
    public Map<String, Object> accountLogin(String account, String password) {
        // 首先尝试将账号作为手机号进行查找
        // 尝试手机号查找
        User user = userMapper.selectByPhone(account);
        // 如果手机号未找到，尝试将账号作为用户名进行查找
        // 尝试用户名查找
        if (user == null) {
            // 按用户名查询用户记录
            user = userMapper.selectByUsername(account);
        }
        // 如果两种方式都未找到用户，说明账号未注册
        if (user == null) {
            // 抛出自定义异常，提示账号未注册
            throw new CustomException("账号未注册");
        }
        // 校验密码：密码为空（微信注册用户）或密码不匹配，则登录失败
        if (StrUtil.isBlank(user.getPassword()) || !user.getPassword().equals(password)) {
            // 抛出自定义异常，提示密码错误
            throw new CustomException("密码错误");
        }

        // 密码验证通过，生成新的JWT令牌
        String token = jwtUtils.generateToken(user.getId(), user.getOpenid());
        // 将生成的token更新到用户记录中
        user.setToken(token);
        // 持久化更新用户信息到数据库
        userMapper.updateById(user);

        // 创建返回数据的Map容器
        Map<String, Object> data = new HashMap<>();
        // 将用户信息放入返回数据中
        data.put("user", user);
        // 将JWT令牌放入返回数据中
        data.put("token", token);
        // 返回登录成功的用户数据和token
        return data;
    }

    // 手机号密码登录方法，专门通过手机号进行登录验证
    public Map<String, Object> phoneLogin(String phone, String password) {
        // 根据手机号查询用户是否存在
        User user = userMapper.selectByPhone(phone);
        // 判断用户是否为空（该手机号是否已注册）
        if (user == null) {
            // 手机号未注册，抛出自定义异常
            throw new CustomException("手机号未注册");
        }
        // 校验密码：密码为空或密码不匹配则登录失败
        if (StrUtil.isBlank(user.getPassword()) || !user.getPassword().equals(password)) {
            // 密码错误，抛出自定义异常
            throw new CustomException("密码错误");
        }

        // 密码验证通过，生成新的JWT令牌
        String token = jwtUtils.generateToken(user.getId(), user.getOpenid());
        // 将生成的token更新到用户记录中
        user.setToken(token);
        // 持久化更新用户信息到数据库
        userMapper.updateById(user);

        // 创建返回数据的Map容器
        Map<String, Object> data = new HashMap<>();
        // 将用户信息放入返回数据中
        data.put("user", user);
        // 将JWT令牌放入返回数据中
        data.put("token", token);
        // 返回登录成功的用户数据和token
        return data;
    }

    // 手机号注册方法，使用手机号和密码创建新用户账户
    public Map<String, Object> register(String phone, String password) {
        // 首先检查该手机号是否已经被注册过
        // 检查手机号是否已注册
        User existing = userMapper.selectByPhone(phone);
        // 判断是否已存在使用该手机号的用户
        if (existing != null) {
            // 手机号已被注册，抛出自定义异常
            throw new CustomException("该手机号已被注册");
        }

        // 创建新的用户实体对象
        User user = new User();
        // 设置用户名为手机号（手机号作为登录账号）
        user.setUsername(phone);
        // 设置用户密码
        user.setPassword(password);
        // 设置默认昵称："用户" + 手机号后4位（如"用户1234"）
        user.setName("用户" + phone.substring(phone.length() - 4));
        // 设置用户角色为"普通用户"
        user.setRole("普通用户");
        // 设置用户手机号
        user.setPhone(phone);
        // 初始化账户余额为0
        user.setAccount(BigDecimal.ZERO);
        // 设置默认头像为空字符串
        user.setAvatar("");
        // 将新注册的用户信息插入数据库
        userMapper.insert(user);

        // 重新查询刚插入的用户，获取数据库自动生成的用户ID
        user = userMapper.selectByPhone(phone);
        // 生成JWT令牌，包含用户ID和openid（openid可能为null，表示非微信用户）
        String token = jwtUtils.generateToken(user.getId(), user.getOpenid());
        // 将生成的token更新到用户记录中
        user.setToken(token);
        // 持久化更新用户信息到数据库
        userMapper.updateById(user);

        // 创建返回数据的Map容器
        Map<String, Object> data = new HashMap<>();
        // 将用户信息放入返回数据中
        data.put("user", user);
        // 将JWT令牌放入返回数据中
        data.put("token", token);
        // 返回注册成功的用户数据和token
        return data;
    }

    // 为微信登录用户绑定手机号和设置密码的方法
    public void bindPhone(Integer userId, String phone, String password) {
        // 首先检查该手机号是否已被其他用户绑定使用
        // 校验手机号是否已被其他人使用
        User existUser = userMapper.selectByPhone(phone);
        // 判断是否存在该手机号，且该手机号不属于当前用户本人
        if (existUser != null && !existUser.getId().equals(userId)) {
            // 手机号已被其他用户绑定，抛出自定义异常
            throw new CustomException("该手机号已被其他用户绑定");
        }
        // 校验密码是否为空或长度不足6位
        if (StrUtil.isBlank(password) || password.length() < 6) {
            // 密码不符合要求，抛出自定义异常
            throw new CustomException("密码至少6位");
        }

        // 根据用户ID查询当前用户信息
        User user = userMapper.selectById(userId);
        // 将手机号设置到用户实体中
        user.setPhone(phone);
        // 将密码设置到用户实体中
        user.setPassword(password);
        // 更新用户信息到数据库，完成手机号和密码的绑定
        userMapper.updateById(user);
    }
}
