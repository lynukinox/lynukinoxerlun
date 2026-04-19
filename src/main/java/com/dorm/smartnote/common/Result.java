package com.dorm.smartnote.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;    // 状态码：200-成功，401-未登录，500-系统错误
    private String msg;      // 提示信息
    private T data;          // 返回的数据

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.data = data;
        r.msg = "操作成功";
        return r;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.msg = msg;
        return r;
    }
}