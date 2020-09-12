package com.spring.coupon.constant;

public class Constant {
    public static final String TOPIC = "user_coupon_op";
    public static class RedisPrefix {
        public static final String COUPON_TEMPLATE = "coupon_template_code_";
        public static final String USER_COUPON_USABLE = "user_coupon_usable_";
        public static final String USER_COUPON_USED = "user_coupon_used_";
        public static final String USER_COUPON_EXPIRED = "user_coupon_expired_";
    }
}
