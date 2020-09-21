package com.spring.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RuleFlag {
    REBATE("rebate rule"),
    DISCOUNT("discount rule"),
    DEDUCTION("deduction rule"),
    REBATE_DISCOUNT("rebate+discount rule");

    private String description;
}
