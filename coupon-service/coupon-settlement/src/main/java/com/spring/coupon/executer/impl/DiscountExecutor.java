package com.spring.coupon.executer.impl;

import com.spring.coupon.constant.RuleFlag;
import com.spring.coupon.executer.AbstractExecutor;
import com.spring.coupon.executer.IRuleExecutor;
import com.spring.coupon.vo.CouponTemplateSDK;
import com.spring.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DiscountExecutor extends AbstractExecutor implements IRuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.DISCOUNT;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double sum = twoDecimals(
                calculate(settlement.getGoodsInfos())
        );
        SettlementInfo probability = processGoodsTypeNotSatisfy(
                settlement, sum
        );
        if (null != probability) {
            log.debug("Discount template is not match goods type");
            return probability;
        }
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplate();

        double quota = (double) templateSDK.getRule().getDiscount().getQuota();

        settlement.setCost(
                Math.max(twoDecimals((sum * (quota * 1.0/100))), minCost())
        );
        log.debug("Use discount coupon");
        return settlement;
    }
}
