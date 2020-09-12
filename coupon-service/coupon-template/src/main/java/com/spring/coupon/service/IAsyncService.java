package com.spring.coupon.service;

import com.spring.coupon.entity.CouponTemplate;

public interface IAsyncService {
    void asyncConstructCouponByTemplate(CouponTemplate template);
}
