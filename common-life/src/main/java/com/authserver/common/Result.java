package com.authserver.common;

import lombok.Data;

/**
 * 公共返回参数信息
 */
@Data
public class Result<T> {

    public static final String ERROR = "403";
    public static final String SUCCESS = "200";
    public static final String SUCCESS_DESC = "操作成功";
    private String code;
    private String desc;
    private T data;


    public Result(String code, String desc, T data) {
        this.code = code;
        this.desc = desc;
        this.data = data;
    }

    public static <T> Result<T> ok(String desc, T data) {
        return new Result<>(SUCCESS, desc, data);
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS, SUCCESS_DESC, data);
    }

}
