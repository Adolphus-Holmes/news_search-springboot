package com.news.api.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExcHandler {
    /**
     * @description:处理由断言，IllegalArgumentException抛出得异常信息
     * @return java.lang.String
     */
    @ResponseBody
    @ExceptionHandler(value = IllegalArgumentException.class)
    public String handleArgError(IllegalArgumentException e,HttpServletResponse response){
        switch (e.getMessage()){
            case "公钥已过期":
                response.setStatus(408);//超时
            case "请求头与XSRF-TOKEN不一致":
                response.setStatus(403);//拒绝执行
            case "Token已过期":
                response.setStatus(401);//要求身份认证
        }
        return e.getMessage();
    }
}
