package com.spring.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    ADMIN("admin"),
    SUPER_ADMIN("super admin"),
    CUSTOMER("customer");

    private String roleName;
}
