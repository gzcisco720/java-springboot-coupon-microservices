package com.spring.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpModeEnum {
    READ("Read"),
    WRITE("Write");

    private String mode;
}
