package com.soko.minifirfin.common.exception;

public enum BadRequestCode {
    ERROR_CODE_NOT_FOUND(0000, "해당 에러의 에러코드를 찾을 수 없습니다.");

    private int code;
    private String message;

    BadRequestCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
