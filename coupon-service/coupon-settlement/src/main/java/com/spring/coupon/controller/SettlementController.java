package com.spring.coupon.controller;

import com.spring.coupon.exception.CouponException;
import com.spring.coupon.executer.ExecutorManager;
import com.spring.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SettlementController {

    private final ExecutorManager executorManager;

    public SettlementController(ExecutorManager executorManager) {
        this.executorManager = executorManager;
    }

    @PostMapping("/settlement/compute")
    public SettlementInfo computeRule(@RequestBody SettlementInfo settlement)
            throws CouponException {
        return executorManager.computeRule(settlement);
    }
}
