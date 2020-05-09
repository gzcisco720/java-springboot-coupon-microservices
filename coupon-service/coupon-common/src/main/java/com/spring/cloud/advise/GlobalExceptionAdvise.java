package com.spring.cloud.advise;

import com.spring.cloud.exception.CouponException;
import com.spring.cloud.vo.CommonResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionAdvise {

    @ExceptionHandler(value = CouponException.class)
    public CommonResponse<String> handlerCouponException(
            HttpServletRequest req, CouponException ex
    ){
        CommonResponse<String> response = new CommonResponse<>(
                -1, "business error"
        );
        response.setData(ex.getMessage());
        return response;
    }
}
