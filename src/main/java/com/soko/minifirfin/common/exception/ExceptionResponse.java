package com.soko.minifirfin.common.exception;

public class ExceptionResponse {
    private int code;
    private String message;

    public ExceptionResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public ExceptionResponse(BadRequestException badRequestException) {
        this(badRequestException.getCode(), badRequestException.getMessage());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
