package com.wll.common.exception; // 声明包路径，exception包专门存放异常类

/**
 * 自定义业务异常类
 * 继承RuntimeException（运行时异常），这样抛出时不强制调用方try-catch
 * 当业务逻辑校验不通过时（如用户名不存在、密码错误、库存不足等），
 * 服务层抛出此异常，由GlobalExceptionHandler统一拦截并返回给前端
 *
 * 使用示例：
 *   throw new CustomException("库存不足");
 *   前端收到：{"code":"500","msg":"库存不足","data":null}
 */
public class CustomException extends RuntimeException { // 继承RuntimeException，可以不强制try-catch

    /** 异常消息，存储具体的错误描述文字 */
    private String msg;

    /**
     * 构造函数
     * @param msg 错误消息文本，会原样返回给前端
     */
    public CustomException(String msg) {
        super(msg); // 调用父类RuntimeException的构造函数，设置异常消息
        this.msg = msg; // 同时存储到自己的msg字段
    }

    /** 获取错误消息 */
    public String getMsg() {
        return msg; // 返回存储的错误消息
    }

    /** 设置错误消息 */
    public void setMsg(String msg) {
        this.msg = msg; // 更新错误消息
    }
}
