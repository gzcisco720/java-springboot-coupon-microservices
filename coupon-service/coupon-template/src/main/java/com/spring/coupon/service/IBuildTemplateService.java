package com.spring.coupon.service;

import com.spring.coupon.vo.TemplateRequest;
import com.spring.coupon.entity.CouponTemplate;
import com.spring.coupon.exception.CouponException;

public interface IBuildTemplateService {
    CouponTemplate buildTemplate(TemplateRequest request)
            throws CouponException;
}
