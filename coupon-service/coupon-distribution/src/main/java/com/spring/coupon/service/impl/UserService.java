package com.spring.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.spring.coupon.constant.Constant;
import com.spring.coupon.constant.CouponStatus;
import com.spring.coupon.dao.CouponDao;
import com.spring.coupon.entity.Coupon;
import com.spring.coupon.exception.CouponException;
import com.spring.coupon.feign.SettlementClient;
import com.spring.coupon.feign.TemplateClient;
import com.spring.coupon.service.IRedisService;
import com.spring.coupon.service.IUserService;
import com.spring.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService implements IUserService {

    private final CouponDao couponDao;

    private final IRedisService redisService;

    private final TemplateClient templateClient;

    private final SettlementClient settlementClient;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public UserService(CouponDao couponDao,
                       IRedisService redisService,
                       @Qualifier("eureka-client-coupon-template") TemplateClient templateClient,
                       @Qualifier("eureka-client-coupon-settlement") SettlementClient settlementClient,
                       KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status)
            throws CouponException {

        List<Coupon> currentCached = redisService.getCachedCoupons(userId,status);
        List<Coupon> preTarget;

        if(CollectionUtils.isNotEmpty(currentCached)) {
            log.debug("coupon cache is not empty: {} {}", userId, status);
            preTarget = currentCached;
        } else {
            log.debug("coupon cache is empty, get coupon from db: {} {}", userId, status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(
                    userId, CouponStatus.of(status)
            );
            if (CollectionUtils.isNotEmpty(dbCoupons)) {
                log.debug("current user does not have coupon: {}", userId);
                return dbCoupons;
            }
            Map<Integer, CouponTemplateSDK> id2TemplateSDK =
                    templateClient.findIds2TemplateSDK(
                            dbCoupons.stream()
                                    .map(Coupon::getTemplateId)
                                    .collect(Collectors.toList())
                    ).getData();
            dbCoupons.forEach(
                    dc -> dc.setTemplateSDK(
                            id2TemplateSDK.get(
                                    dc.getTemplateId()
                            )
                    )
            );
            preTarget = dbCoupons;
            redisService.addCouponToCache(userId, preTarget, status);
        }

        preTarget = preTarget.stream()
                .filter(c -> c.getId() != -1)
                .collect(Collectors.toList());

        if (CouponStatus.of(status) == CouponStatus.USABLE) {
            CouponClassify classify = CouponClassify.classify(preTarget);
            if(CollectionUtils.isNotEmpty(classify.getExpired())) {
                log.info("Add expired coupons to cache {}, {}", userId, userId);
                redisService.addCouponToCache(
                        userId,
                        classify.getExpired(),
                        CouponStatus.EXPIRED.getCode()
                );
                kafkaTemplate.send(
                        Constant.TOPIC,
                        JSON.toJSONString(new CouponKafkaMessage(
                                CouponStatus.EXPIRED.getCode(),
                                classify.getExpired().stream()
                                        .map(Coupon::getId).collect(Collectors.toList())
                        ))
                );
            }
            return classify.getUsable();
        }

        return preTarget;
    }

    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId)
            throws CouponException {
        return null;
    }

    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request)
            throws CouponException {
        return null;
    }

    @Override
    public SettlementInfo settlement(SettlementInfo info)
            throws CouponException {
        return null;
    }
}
