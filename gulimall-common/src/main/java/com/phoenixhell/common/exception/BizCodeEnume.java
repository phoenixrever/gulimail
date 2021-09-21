package com.phoenixhell.common.exception;


/*错误码列表
    10 通用
        001 参数格式校验
        002 短信验证码频率太高
    11 商品
    12 订单
    13 购物车
    14 物流
    15 用户
    21 库存
    22 feign
    */
public enum BizCodeEnume {
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    TO_MANY_REQUEST(10002, "请求过于频繁"),
    VALID_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODe_EXCEPTION(10001, "验证码获取频率太高请稍后再试"),
    USER_EXIST_EXCEPTION(15001, "用户名已经存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已经存在"),
    NO_STOCK_EXCEPTION(21000, "商品库存不足"),
    TOKEN_NOT_MATCH_EXCEPTION(21001, "令牌校验失败"),
    PRICE_NOT_MATCH_EXCEPTION(21002, "价格校验失败"),
    LOGINACCOUNT_PASSWORD_EXCEPTION(150303, "账号或者密码错误"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    FEIGN_EXCEPTION(22000, "feign远程服务调用失败");

    private Integer code;
    private String msg;

    BizCodeEnume(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }


    public String getMsg() {
        return msg;
    }
}
