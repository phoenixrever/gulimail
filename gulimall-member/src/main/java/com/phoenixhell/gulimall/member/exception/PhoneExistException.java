package com.phoenixhell.gulimall.member.exception;

public class PhoneExistException extends RuntimeException {
    public PhoneExistException() {
        super("手机已经存在");
    }
}
