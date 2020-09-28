package com.spring.coupon.service.impl;

import com.google.common.base.Stopwatch;
import com.spring.coupon.constant.Constant;
import com.spring.coupon.dao.CouponTemplateRepository;
import com.spring.coupon.entity.CouponTemplate;
import com.spring.coupon.service.IAsyncService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AsyncServiceImpl implements IAsyncService {

    private final CouponTemplateRepository templateDao;
    private final StringRedisTemplate redisTemplate;

    public AsyncServiceImpl(CouponTemplateRepository templateDao, StringRedisTemplate redisTemplate) {
        this.templateDao = templateDao;
        this.redisTemplate = redisTemplate;
    }

    @SuppressWarnings("all")
    @Async("getAsyncExecutor")
    @Override
    public void asyncConstructCouponByTemplate(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();

        Set<String> couponCodes = buildCouponCode(template);

        String redisKey = String.format("%s%s",
                Constant.RedisPrefix.COUPON_TEMPLATE, template.getId().toString());
        log.info("Push Coupon code to Redis", redisTemplate.opsForList().rightPushAll(redisKey, couponCodes));
        template.setAvailable(true);
        templateDao.save(template);
        watch.stop();
        log.info("Constract Coupon cost: {}ms", watch.elapsed(TimeUnit.MILLISECONDS));
    }

    @SuppressWarnings("all")
    private Set<String> buildCouponCode(CouponTemplate template) {
        Stopwatch watch = Stopwatch.createStarted();
        Set<String> result = new HashSet<>(template.getCount());
        String prefix4 = template.getProductLine().getCode().toString() + template.getCategory().getCode().toString();
        String date = new SimpleDateFormat("yyMMdd").format(template.getCreatedTime());

        for (int i=0; i != template.getCount(); ++i) {
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }

        while (result.size() < template.getCount()) {
            result.add(prefix4 + buildCouponCodeSuffix14(date));
        }

        assert result.size() == template.getCount();

        watch.stop();

        log.info("Build template cost: {}ms", watch.elapsed(TimeUnit.MILLISECONDS));

        return result;
    }

    private String buildCouponCodeSuffix14(String date) {
        char[] bases = new char[]{'1','2','3','4','5','6','7','8','9'};
        List<Character> chars = date.chars().mapToObj(e -> (char) e).collect(Collectors.toList());
        Collections.shuffle(chars);
        String mid6 = chars.stream().map(Objects::toString).collect(Collectors.joining());
        String suffix8 = RandomStringUtils.random(1, bases)
                + RandomStringUtils.randomNumeric(7);
        return mid6 + suffix8;
    }
}
