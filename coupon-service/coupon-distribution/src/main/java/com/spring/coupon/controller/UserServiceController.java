package com.spring.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.spring.coupon.entity.Coupon;
import com.spring.coupon.exception.CouponException;
import com.spring.coupon.service.IUserService;
import com.spring.coupon.vo.AcquireTemplateRequest;
import com.spring.coupon.vo.CouponTemplateSDK;
import com.spring.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class UserServiceController {

    private final IUserService userService;

    public UserServiceController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/coupons")
    public List<Coupon> findCouponByStatus(
            @RequestParam("userId") Long userId,
            @RequestParam("status") Integer status
    ) throws CouponException {
        log.info("Find coupons by status: {}, {}", userId, status);
        return userService.findCouponsByStatus(userId,status);
    }

    @GetMapping("/template")
    public List<CouponTemplateSDK> findAvailableTemplate(
            @RequestParam("userId") Long userId
    ) throws CouponException {
        log.info("Find available template: {}", userId);
        return userService.findAvailableTemplate(userId);
    }

    @PostMapping("/acquire/template")
    public Coupon acquireTemplate(
            @RequestBody AcquireTemplateRequest request
    ) throws CouponException {
        log.info("Acquire Template: {}", JSON.toJSONString(request));
        return userService.acquireTemplate(request);
    }

    @PostMapping("/settlement")
    public SettlementInfo settlement(
            @RequestBody SettlementInfo info
    ) throws CouponException {
        log.info("Settlement: {}", JSON.toJSONString(info));
        return userService.settlement(info);
    }

}
