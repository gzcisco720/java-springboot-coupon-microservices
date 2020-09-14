package com.spring.coupon.feign.hystrix;

import com.spring.coupon.feign.TemplateClient;
import com.spring.coupon.vo.CommonResponse;
import com.spring.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {
    @Override
    public CommonResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate request error");
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] findAllUsableTemplate request error",
                Collections.emptyList()
        );
    }

    @Override
    public CommonResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(Collection<Integer> ids) {
        log.error("[eureka-client-coupon-template] findIds2TemplateSDK request error");
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-template] findIds2TemplateSDK request error",
                new HashMap<>()
        );
    }
}
