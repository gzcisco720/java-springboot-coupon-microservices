package com.spring.coupon.service;

import com.spring.coupon.vo.AcquireTemplateRequest;
import com.spring.coupon.entity.Coupon;
import com.spring.coupon.exception.CouponException;
import com.spring.coupon.vo.CouponTemplateSDK;
import com.spring.coupon.vo.SettlementInfo;

import java.util.List;

public interface IUserService {
    List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException;
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;
    SettlementInfo settlement(SettlementInfo info) throws CouponException;
}
