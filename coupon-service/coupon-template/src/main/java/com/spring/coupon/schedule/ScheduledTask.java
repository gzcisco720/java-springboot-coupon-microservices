package com.spring.coupon.schedule;

import com.spring.coupon.dao.CouponTemplateRepository;
import com.spring.coupon.entity.CouponTemplate;
import com.spring.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class ScheduledTask {
    private final CouponTemplateRepository templateDao;

    public ScheduledTask(CouponTemplateRepository templateDao) {
        this.templateDao = templateDao;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void offlineCouponTemplate() {
        log.info("Start to expire couponTemplate");
        List<CouponTemplate> templates =
                templateDao.findAllByExpired(false);
        if (CollectionUtils.isEmpty(templates)) {
            log.info("Done to expire couponTemplate");
            return;
        }
        Date current = new Date();
        List<CouponTemplate> expiredTemplates = new ArrayList<>(templates.size());
        templates.forEach(t -> {
            TemplateRule rule = t.getRule();
            if (rule.getExpiration().getDeadline() < current.getTime()) {
                t.setExpired(true);
            }
            expiredTemplates.add(t);
        });
        if (CollectionUtils.isNotEmpty(expiredTemplates)) {
            log.info("Expired CouponTemplate Number: {}", templateDao.saveAll(expiredTemplates));
        }
    }
}
