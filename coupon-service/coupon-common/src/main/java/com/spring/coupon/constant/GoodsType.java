package com.spring.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

@Getter
@AllArgsConstructor
public enum GoodsType {
    ENTERTAINMENT("Entertainment", 1),
    FOOD("Food", 2),
    HOME("Home", 3),
    OTHERS("Others",4),
    ALL("All", 5);

    private String description;
    private Integer code;

    public GoodsType of(Integer code) {
        Objects.requireNonNull(code);

        return Stream.of(values())
                .filter(bean -> bean.code.equals(code))
                .findAny()
                .orElseThrow(()->new IllegalArgumentException(code+" not exists"));
    }
}
