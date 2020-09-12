package com.spring.coupon.service;

import com.spring.coupon.entity.CouponTemplate;
import com.spring.coupon.exception.CouponException;
import com.spring.coupon.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ITemplateBaseService {
     CouponTemplate buildTemplateInfo(Integer id) throws CouponException;

     List<CouponTemplateSDK> findAllUsableTemplate();

     Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);
}
