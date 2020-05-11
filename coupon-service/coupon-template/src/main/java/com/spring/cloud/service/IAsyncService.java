package com.spring.cloud.service;

import com.spring.cloud.entity.CouponTemplate;

public interface IAsyncService {
    void asyncConstructCouponByTemplate(CouponTemplate template);
}
