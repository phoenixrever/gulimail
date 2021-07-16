package com.phoenixhell.gulimall.product.exception;

import com.phoenixhell.common.exception.BizCodeEnume;
import com.phoenixhell.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@Slf4j
//@ResponseBody
//@ControllerAdvice(basePackages = "com.phoenixhell.gulimall.product.controller")
@RestControllerAdvice(basePackages = "com.phoenixhell.gulimall.product.controller")
public class ExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidationException(MethodArgumentNotValidException e) {
        log.error("validate", e);
        BindingResult bindingResult = e.getBindingResult();
        HashMap<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach(item -> {
            errorMap.put(item.getField(), item.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    //精确匹配不到的错误 最后来到这
    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("error===>", throwable.getMessage());
        return R.error(BizCodeEnume.UNKNOWN_EXCEPTION.getCode(),BizCodeEnume.UNKNOWN_EXCEPTION.getMsg()).put("data",throwable.getMessage());

    }

//    //精确匹配不到的错误 最后来到这
//    @ExceptionHandler(value = Exception.class)
//    public R handleException(Exception e) {
//        return R.error(BizCodeEnume.UNKNOWN_EXCEPTION.getCode(),BizCodeEnume.UNKNOWN_EXCEPTION.getMsg()).put("data",e.getMessage());
//
//    }
}
