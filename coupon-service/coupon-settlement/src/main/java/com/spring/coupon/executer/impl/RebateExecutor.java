package com.spring.coupon.executer.impl;

import com.spring.coupon.constant.RuleFlag;
import com.spring.coupon.executer.AbstractExecutor;
import com.spring.coupon.executer.IRuleExecutor;
import com.spring.coupon.vo.CouponTemplateSDK;
import com.spring.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Slf4j
@Component
public class RebateExecutor extends AbstractExecutor implements IRuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.REBATE;
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
            log.debug("Rebate template is not match goods type");
            return probability;
        }

        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos()
                .get(0).getTemplate();

        double base = (double) templateSDK.getRule().getDiscount().getBase();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();

        if (sum < base) {
            log.debug("sum < base");
            settlement.setCost(sum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        settlement.setCost(twoDecimals(
                Math.max((sum - quota), minCost())
        ));

        log.debug("Use Rebate coupon");

        return settlement;
    }
}
