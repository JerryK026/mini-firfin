package com.soko.minifirfin.common.exception;

public class BadRequestException extends RuntimeException {

    private final String message;
    private final int code;

    public BadRequestException(BadRequestCode badRequestCode) {
        this.message = badRequestCode.getMessage();
        this.code = badRequestCode.getCode();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getCode() {
        return code;
    }
}
