package com.news.api.util;

public class XSRFException extends SecurityException {
    private Integer code;
    private String message;

    public XSRFException(Integer code,String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
