package com.soko.minifirfin.common.exception;

public enum BadRequestCode {
    ERROR_CODE_NOT_FOUND(0000, "해당 에러의 에러코드를 찾을 수 없습니다."),

    MEMBER_NOT_FOUND(1000, "해당 id를 가진 멤버가 존재하지 않습니다."),

    NOT_ENOUGH_MONEY(2000, "잔액이 모자랍니다."),
    DIFFERENT_CURRENCY(2001, "통화가 다릅니다."),
    NEGATIVE_MONEY_NOT_ALLOWED(2002, "금액은 음수일 수 없습니다."),
    RECEIVER_OVER_LIMITATION(2002, "받는 사람의 잔액 한도를 초과합니다."),
    SENDER_AND_RECEIVER_ARE_SAME(2003, "보내는 사람과 받는 사람은 같은 사람일 수 없습니다."),
    SENDER_OVER_DAILY_LIMITATION(2004, "1일 송금 한도를 초과합니다. 1일 송금 한도 : 199만원"),
    SENDER_OVER_ONCE_LIMITATION(2005, "1회 송금 한도를 초과합니다. 1회 송금 한도 : 199만원"),
    RECHARGER_OVER_LIMITATION(3000, "충전시 한도를 초과합니다."),
    RECHARGE_OVER_DAILY_LIMITATION(3001, "1일 충전 한도를 초과합니다. 1일 충전 한도 : 2999만원"),
    RECHARGE_OVER_ONCE_LIMITATION(3002, "1회 충전 한도를 초과합니다. 1회 충전 한도 : 599만원");

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
