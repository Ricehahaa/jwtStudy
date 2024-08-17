package com.example.utils;

public class Const {
    //用于退出登录的黑名单
    public static final String JWT_BLACK_LIST = "jwt:blacklist:";
    //跨域filter
    //越小,filter越先处理
    public static final int ORDER_CORS = -102;
    //限流filter
    public static final int ORDER_LIMIT = -101;
    //用于验证码的限流
    public static final String VERIFY_EMAIL_LIMIT = "verify:email:limit:";
    //用于验证验证码是否正确
    public static final String VERIFY_EMAIL_DATA = "verify:email:data:";
    //短时间内请求的次数，用于限流
    public static final String FLOW_LIMIT_COUNTER = "flow:counter";
    //短时间请求过多,限流的标志
    public static final String FLOW_LIMIT_BLOCK = "flow:block";

}
