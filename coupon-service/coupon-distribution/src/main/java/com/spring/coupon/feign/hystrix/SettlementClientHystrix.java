package com.spring.coupon.feign.hystrix;

import com.spring.coupon.exception.CouponException;
import com.spring.coupon.feign.SettlementClient;
import com.spring.coupon.vo.CommonResponse;
import com.spring.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementClientHystrix implements SettlementClient {
    @Override
    public CommonResponse<SettlementInfo> computedRule(SettlementInfo settlement)
            throws CouponException {
        log.error("[eureka-client-coupon-settlement] computedRule request error");
        settlement.setEmploy(false);
        settlement.setCost(-1.0);
        return new CommonResponse<>(
                -1,
                "[eureka-client-coupon-settlement] computedRule request error",
                settlement
        );
    }
}
