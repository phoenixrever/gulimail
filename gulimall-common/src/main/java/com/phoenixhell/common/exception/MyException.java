package com.phoenixhell.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyException extends RuntimeException {
    private Integer code; //状态码
    private String msg;//异常信息
}
