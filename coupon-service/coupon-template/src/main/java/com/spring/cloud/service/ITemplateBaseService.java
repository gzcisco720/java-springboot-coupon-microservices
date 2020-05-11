package com.spring.cloud.service;

import com.spring.cloud.entity.CouponTemplate;
import com.spring.cloud.exception.CouponException;
import com.spring.cloud.vo.CouponTemplateSDK;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ITemplateBaseService {
     CouponTemplate buildTemplateInfo(Integer id) throws CouponException;

     List<CouponTemplateSDK> findAllUsableTemplate();

     Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);
}
