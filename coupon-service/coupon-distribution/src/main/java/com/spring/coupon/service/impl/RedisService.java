package com.spring.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.spring.coupon.constant.Constant;
import com.spring.coupon.constant.CouponStatus;
import com.spring.coupon.entity.Coupon;
import com.spring.coupon.exception.CouponException;
import com.spring.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@SuppressWarnings("all")
public class RedisService implements IRedisService {
    private final StringRedisTemplate redisTemplate;

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<Coupon> getCachedCoupons(Long userId, Integer status) {
        log.info("Get coupon from cache: {}, {}", userId, status);
        String redisKey = status2RedisKey(status, userId);

        List<String> couponStrs = redisTemplate.opsForHash()
                .values(redisKey)
                .stream()
                .map(o -> Objects.toString(o, null))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(couponStrs)) {
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }
        return couponStrs.stream()
                .map(c -> JSON.parseObject(c, Coupon.class))
                .collect(Collectors.toList());
    }

    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        log.info("Save empty list to cache user: {}, status: {}", userId, JSON.toJSONString(status));
        Map<String, String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1", JSON.toJSONString(Coupon.invalidCoupon()));
        SessionCallback sessionCallback = new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations)
                    throws DataAccessException {
                status.forEach(s -> {
                    String redisKey = status2RedisKey(s, userId);
                    redisOperations.opsForHash().putAll(redisKey, invalidCouponMap);
                });
                return null;
            }
        };
        log.info("Redis pipeline result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
    }

    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s", Constant.RedisPrefix.COUPON_TEMPLATE, templateId.toString());
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);
        log.info("Acquire coupon code: {} {} {}", templateId, redisKey, couponCode);
        return couponCode;
    }

    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons,
                                    Integer status) throws CouponException {
        log.info("Add coupon to cache: {} {} {}", userId, JSON.toJSONString(coupons), status);
        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);

        switch (couponStatus) {
            case USABLE:
                result = addCouponToCacheForUsable(userId, coupons);
                break;
            case USED:
                result = addCouponToCacheForUsed(userId, coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(userId, coupons);
                break;
        }

        return result;
    }

    private Integer addCouponToCacheForUsable(Long userId, List<Coupon> coupons) {
        log.info("Add coupon to cache for usable");
        Map<String, String> needCachedObject = new HashMap<>();
        coupons.forEach(c -> {
            needCachedObject.put(c.getId().toString(), JSON.toJSONString(c));
        });
        String redisKey = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        redisTemplate.opsForHash().putAll(redisKey, needCachedObject);
        log.info("Add coupons to cache: {}, {}, {}", needCachedObject.size(), userId, redisKey);
        redisTemplate.expire(redisKey, getRandomExpirationTime(1, 2), TimeUnit.SECONDS);
        return needCachedObject.size();
    }


    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> coupons)
            throws CouponException{
        log.info("Add coupon to cache for used");
        Map<String, String> needCachedForUsed = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForUsed = status2RedisKey(CouponStatus.USED.getCode(), userId);
        List<Coupon> currentUsableCoupons = getCachedCoupons(userId, CouponStatus.USABLE.getCode());
        assert currentUsableCoupons.size() > coupons.size();
        coupons.forEach(c -> {
            needCachedForUsed.put(c.getId().toString(), JSON.toJSONString(c));
        });
        List<Integer> currentUsableIds = currentUsableCoupons.stream()
                                            .map(Coupon::getId)
                                            .collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                                    .map(Coupon::getId)
                                    .collect(Collectors.toList());
        if(!CollectionUtils.isSubCollection(paramIds, currentUsableIds)) {
            log.error("CurrentCoupons is not equal to cache: {} {} {}", userId, JSON.toJSONString(currentUsableIds), JSON.toJSONString(paramIds));
            throw new CouponException("CurrentCoupons is not equal to cache");
        }
        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.opsForHash().putAll(redisKeyForUsed, needCachedForUsed);
                redisOperations.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());
                redisOperations.expire(redisKeyForUsable, getRandomExpirationTime(1,2), TimeUnit.SECONDS);
                redisOperations.expire(redisKeyForUsed, getRandomExpirationTime(1,2), TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Pipeline execute result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> coupons)
            throws CouponException{
        log.info("Add coupon to cache for expired");
        Map<String, String> needCachedForExpired = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(CouponStatus.USABLE.getCode(), userId);
        String redisKeyForExpired = status2RedisKey(CouponStatus.EXPIRED.getCode(), userId);
        List<Coupon> currentUsableCoupons = getCachedCoupons(userId, CouponStatus.USABLE.getCode());
        List<Coupon> currentExpiredCoupons = getCachedCoupons(userId, CouponStatus.EXPIRED.getCode());
        assert currentUsableCoupons.size() > coupons.size();
        coupons.forEach(c -> {
            needCachedForExpired.put(c.getId().toString(), JSON.toJSONString(c));
        });

        List<Integer> currentUsableIds = currentUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        if(!CollectionUtils.isSubCollection(paramIds, currentUsableIds)) {
            log.error("CurrentCoupons is not equal to cache: {} {} {}", userId, JSON.toJSONString(currentUsableIds), JSON.toJSONString(paramIds));
            throw new CouponException("CurrentCoupons is not equal to cache");
        }
        List<String> needCleanKey = paramIds.stream().map(i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations redisOperations) throws DataAccessException {
                redisOperations.opsForHash().putAll(redisKeyForExpired, needCachedForExpired);
                redisOperations.opsForHash().delete(redisKeyForUsable, needCleanKey.toArray());
                redisOperations.expire(redisKeyForUsable, getRandomExpirationTime(1,2), TimeUnit.SECONDS);
                redisOperations.expire(redisKeyForExpired, getRandomExpirationTime(1,2), TimeUnit.SECONDS);
                return null;
            }
        };
        log.info("Pipeline execute result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    private String status2RedisKey(Integer status, Long userId){
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus) {
            case USABLE:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case USED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
                break;
        }
        return redisKey;
    }

    private Long getRandomExpirationTime(Integer min, Integer max) {
        return RandomUtils.nextLong(min * 60 * 60, max * 60 * 60);
    }
}
