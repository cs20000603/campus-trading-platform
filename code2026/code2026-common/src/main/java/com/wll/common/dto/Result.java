package com.wll.common.dto; // 声明包路径，dto = Data Transfer Object（数据传输对象）

/**
 * 统一响应结果封装类（泛型）
 * 整个项目所有Controller接口都通过此类返回统一格式的JSON给前端
 * 前端只需要判断code是否为"200"即可知道请求是否成功
 * 泛型T表示data字段的具体数据类型，可以是String、List、PageInfo等任意类型
 *
 * 使用示例：
 *   Result.success(data)       → {"code":"200", "msg":"请求成功", "data":{...}}
 *   Result.error("密码错误")    → {"code":"500", "msg":"密码错误", "data":null}
 */
public class Result<T> { // <T>是泛型声明，T可以是任何类型
    /** 状态码，"200"表示成功，"500"表示失败 */
    private String code;
    /** 提示消息，成功时为"请求成功"，失败时为具体错误描述 */
    private String msg;
    /** 返回的数据主体，泛型T，可以是单个对象、列表、分页数据等 */
    private T data;

    /** 私有构造函数，传入数据对象 */
    private Result(T data) {
        this.data = data; // 将传入的数据赋值给data字段
    }

    /** 公开无参构造函数（框架序列化需要） */
    public Result() {
    }

    /**
     * 静态工厂方法：创建成功响应（无数据）
     * @return Result对象，code="200", msg="请求成功"
     */
    public static <T> Result<T> success() { // <T>在static方法上声明泛型
        Result<T> result = new Result<>(); // 创建空的Result对象
        result.setCode("200"); // 设置成功状态码
        result.setMsg("请求成功"); // 设置成功提示信息
        return result; // 返回构建好的Result对象
    }

    /**
     * 静态工厂方法：创建成功响应（带数据）
     * @param data 要返回给前端的数据对象
     * @return Result对象，包含data数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = success(); // 先调用无参success方法创建基础成功响应
        result.setData(data); // 设置要返回的具体数据
        return result;
    }

    /**
     * 静态工厂方法：创建成功响应（带数据和自定义消息）
     * @param data 要返回的数据
     * @param msg 自定义的成功提示信息
     */
    public static <T> Result<T> success(T data, String msg) {
        Result<T> result = success(data); // 先创建带数据的成功响应
        result.setMsg(msg); // 覆盖默认的"请求成功"消息
        return result;
    }

    /**
     * 静态工厂方法：创建失败响应（无数据）
     * @return Result对象，code="500", msg="请求失败"
     */
    public static <T> Result<T> error() {
        Result<T> result = new Result<>(); // 创建空的Result对象
        result.setCode("500"); // 设置失败状态码
        result.setMsg("请求失败"); // 设置默认失败提示
        return result;
    }

    /**
     * 静态工厂方法：创建失败响应（带自定义错误消息）
     * @param msg 具体的错误描述，如"密码错误"、"用户不存在"
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>(); // 创建空的Result对象
        result.setCode("500"); // 设置失败状态码
        result.setMsg(msg); // 设置自定义错误消息
        return result;
    }

    // ===== 以下为 Getter/Setter 方法（JSON序列化需要） =====
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
