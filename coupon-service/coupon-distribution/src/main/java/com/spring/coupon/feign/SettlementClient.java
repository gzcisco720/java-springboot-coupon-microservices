package com.spring.coupon.feign;

import com.spring.coupon.exception.CouponException;
import com.spring.coupon.feign.hystrix.SettlementClientHystrix;
import com.spring.coupon.vo.CommonResponse;
import com.spring.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(value = "eureka-client-coupon-settlement", fallback = SettlementClientHystrix.class)
public interface SettlementClient {

    @RequestMapping(value = "/coupon-settlement/settlement/compute", method = RequestMethod.POST)
    CommonResponse<SettlementInfo> computedRule(
            @RequestBody SettlementInfo settlement) throws CouponException;
}
