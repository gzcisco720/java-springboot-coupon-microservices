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
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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
                       TemplateClient templateClient,
                       SettlementClient settlementClient,
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
        long currentTime = new Date().getTime();
        List <CouponTemplateSDK> templateSDKS =
                templateClient.findAllUsableTemplate().getData();
        log.debug("Find all template count: {}", templateSDKS.size());
        templateSDKS = templateSDKS.stream()
                .filter(t -> t.getRule().getExpiration().getDeadline() > currentTime)
                .collect(Collectors.toList());
        log.info("Find usable template count: {}", templateSDKS.size());

        // key -> templateId
        // Pair key -> limitation
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template =
                new HashMap<>(templateSDKS.size());
        templateSDKS.forEach(t -> limit2Template.put(
                t.getId(), Pair.of(t.getRule().getLimitation(), t)
        ));
        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        List<Coupon> userUsableCoupons = findCouponsByStatus(userId, CouponStatus.USABLE.getCode());

        log.debug("Current user has usable coupons: {} {}", userId, userUsableCoupons.size());

        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons
                .stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));

        limit2Template.forEach((k,v) -> {
            Integer limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();
            if (templateId2Coupons.containsKey(k) && templateId2Coupons.get(k).size() >= limitation) {
                return;
            }
            result.add(templateSDK);
        });

        return result;
    }

    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request)
            throws CouponException {
        Map<Integer, CouponTemplateSDK> id2Template = templateClient.findIds2TemplateSDK(Collections.singletonList(
                request.getTemplateSDK().getId()
        )).getData();
        if (id2Template.size()<=0) {
            log.error("Cannot get template from template client: {}", request.getTemplateSDK().getId());
            throw new CouponException("Cannot get template from template client" + request.getTemplateSDK().getId());
        }
        List<Coupon> usableCoupons = findCouponsByStatus(request.getUserId(), CouponStatus.USED.getCode());
        Map<Integer, List<Coupon>> templateId2Coupons = usableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId()) &&
        templateId2Coupons.get(request.getTemplateSDK().getId()).size() >=
        request.getTemplateSDK().getRule().getLimitation()) {
            log.error("Exceed limitation: {}", request.getTemplateSDK().getId());
            throw new CouponException("Exceed limitation: " + request.getTemplateSDK().getId());
        }
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(
                request.getTemplateSDK().getId()
        );
        if (StringUtils.isEmpty(couponCode)) {
            log.error("Cannot get template from template client: {}", request.getTemplateSDK().getId());
            throw new CouponException("Cannot get template from template client" + request.getTemplateSDK().getId());
        }
        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(),
                request.getUserId(),
                couponCode,
                CouponStatus.USABLE
        );
        newCoupon = couponDao.save(newCoupon);

        newCoupon.setTemplateSDK(request.getTemplateSDK());

        redisService.addCouponToCache(request.getUserId(),
                Collections.singletonList(newCoupon),
                CouponStatus.USABLE.getCode());

        return newCoupon;
    }

    @Override
    public SettlementInfo settlement(SettlementInfo info)
            throws CouponException {
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos =
                info.getCouponAndTemplateInfos();
        if (CollectionUtils.isEmpty(ctInfos)) {
            log.error("Empty coupon");
            double goodsSum = 0.0;
            for (GoodsInfo goodsInfo : info.getGoodsInfos()) {
                goodsSum += goodsInfo.getPrice() + goodsInfo.getCount();
            }
            info.setCost(retain2Decimals(goodsSum));
        }

        List<Coupon> coupons = findCouponsByStatus(
                info.getUserId(), CouponStatus.USABLE.getCode()
        );
        Map<Integer, Coupon> id2Coupon = coupons.stream()
                .collect(Collectors.toMap(Coupon::getId, Function.identity()));
        if(MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(
                ctInfos.stream().map(SettlementInfo.CouponAndTemplateInfo::getId)
                        .collect(Collectors.toList()),id2Coupon.keySet()
        )) {
            log.info("{}", id2Coupon.keySet());
            log.error("User coupon is not sub collection");
            throw new CouponException("User coupon is not sub collection");
        }
        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(c -> settleCoupons.add(id2Coupon.get(c.getId())));

        SettlementInfo proceedInfo = settlementClient.computedRule(info).getData();

        if (proceedInfo.getEmploy() && CollectionUtils.isNotEmpty(
                proceedInfo.getCouponAndTemplateInfos()
        )) {
            log.info("Settle user coupon: {}, {}",info.getUserId(), JSON.toJSONString(settleCoupons));
            redisService.addCouponToCache(info.getUserId(), settleCoupons, CouponStatus.USED.getCode());
            kafkaTemplate.send(Constant.TOPIC, JSON.toJSONString(new CouponKafkaMessage(
                    CouponStatus.USED.getCode(),
                    settleCoupons.stream().map(Coupon::getId).collect(Collectors.toList())
            )));
        }
        return proceedInfo;
    }

    private double retain2Decimals(double value) {
        return new BigDecimal(value)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }
}
