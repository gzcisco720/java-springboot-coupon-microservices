package com.spring.coupon.executer;

import com.spring.coupon.constant.CouponCategory;
import com.spring.coupon.constant.RuleFlag;
import com.spring.coupon.exception.CouponException;
import com.spring.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@SuppressWarnings("all")
public class ExecutorManager implements BeanPostProcessor {

    private static Map<RuleFlag, IRuleExecutor> executorIndex =
            new HashMap<>(RuleFlag.values().length);

    public SettlementInfo computeRule (SettlementInfo settlement)
            throws CouponException {
        SettlementInfo result = null;
        if(settlement.getCouponAndTemplateInfos().size() == 1) {
            CouponCategory category = CouponCategory.of(
                    settlement.getCouponAndTemplateInfos().get(0).getTemplate().getCategory()
            );
            switch (category) {
                case REBATE:
                    result = executorIndex.get(RuleFlag.REBATE).computeRule(settlement);
                    break;
                case DISCOUNT:
                    result = executorIndex.get(RuleFlag.DISCOUNT).computeRule(settlement);
                    break;
                case DEDUCTION:
                    result = executorIndex.get(RuleFlag.DEDUCTION).computeRule(settlement);
                    break;
            }
        } else {
            List<CouponCategory> categories  = new ArrayList<>(
                    settlement.getCouponAndTemplateInfos().size()
            );
            settlement.getCouponAndTemplateInfos().forEach(ct -> {
                categories.add(CouponCategory.of(ct.getTemplate().getCategory()));
            });
            if (categories.size() != 2) {
                throw new CouponException("Not support");
            } else {
                if (categories.contains(CouponCategory.DEDUCTION) && categories.contains(CouponCategory.DISCOUNT)) {
                    result = executorIndex.get(RuleFlag.REBATE_DISCOUNT).computeRule(settlement);
                } else {
                    throw new CouponException("Not support");
                }
            }
        }
        return result;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        if (!(bean instanceof IRuleExecutor)) {
            return bean;
        }

        IRuleExecutor executor = (IRuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig();

        if (executorIndex.containsKey(ruleFlag)) {
            throw new IllegalStateException("Duplicate executor registration");
        }

        executorIndex.put(ruleFlag, executor);

        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }
}
