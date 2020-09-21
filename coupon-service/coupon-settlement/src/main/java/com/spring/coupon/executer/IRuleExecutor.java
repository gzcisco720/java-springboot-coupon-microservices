package com.spring.coupon.executer;

import com.spring.coupon.constant.RuleFlag;
import com.spring.coupon.vo.SettlementInfo;

public interface IRuleExecutor {
    RuleFlag ruleConfig();
    SettlementInfo computeRule(SettlementInfo settlement);
}
