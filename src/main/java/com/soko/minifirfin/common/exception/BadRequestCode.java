package com.soko.minifirfin.common.exception;

public enum BadRequestCode {
    ERROR_CODE_NOT_FOUND(0000, "해당 에러의 에러코드를 찾을 수 없습니다."),

    MEMBER_NOT_FOUND(1000, "해당 id를 가진 멤버가 존재하지 않습니다."),

    NOT_ENOUGH_MONEY(2000, "잔액이 모자랍니다."),
    DIFFERENT_CURRENCY(2001, "통화가 다릅니다."),
    OVER_LIMITATION(2002, "받는 사람의 잔액 한도를 초과합니다.");

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
