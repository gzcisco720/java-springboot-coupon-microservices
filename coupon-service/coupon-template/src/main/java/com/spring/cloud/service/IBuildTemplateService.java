package com.spring.cloud.service;

import com.spring.cloud.dto.TemplateRequest;
import com.spring.cloud.entity.CouponTemplate;
import com.spring.cloud.exception.CouponException;

public interface IBuildTemplateService {
    CouponTemplate buildTemplate(TemplateRequest request)
            throws CouponException;
}
