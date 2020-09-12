package com.spring.coupon.service;

import com.spring.coupon.entity.Coupon;
import com.spring.coupon.exception.CouponException;

import java.util.List;

public interface IRedisService {
    List<Coupon> getCachedCoupons(Long userId, Integer status);
    void saveEmptyCouponListToCache(Long userId, List<Integer> status);
    String tryToAcquireCouponCodeFromCache(Integer templateId);
    Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException;
}
