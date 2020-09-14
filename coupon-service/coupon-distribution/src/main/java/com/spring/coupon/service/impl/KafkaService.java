package com.spring.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.spring.coupon.constant.Constant;
import com.spring.coupon.constant.CouponStatus;
import com.spring.coupon.dao.CouponDao;
import com.spring.coupon.entity.Coupon;
import com.spring.coupon.service.IKafkaService;
import com.spring.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class KafkaService implements IKafkaService {

    private final CouponDao couponDao;

    public KafkaService(CouponDao couponDao) {
        this.couponDao = couponDao;
    }

    @Override
    @KafkaListener(topics = {Constant.TOPIC}, groupId = "spring-coupon-x")
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMsg = Optional.ofNullable(record.value());
        if (kafkaMsg.isPresent()) {
            Object message = kafkaMsg.get();
            CouponKafkaMessage couponInfo = JSON.parseObject(
                    message.toString(),
                    CouponKafkaMessage.class
            );
            log.info("Received coupon message: {}", message.toString());
            CouponStatus status = CouponStatus.of(couponInfo.getStatus());
            /*
                USABLE is not needed, because USABLE is a sync process,
                Once user pick a coupon, record need to write into both
                cache and db.
            */
            switch (status) {
                case USABLE:
                    break;
                case USED:
                    processUsedCoupons(couponInfo, status);
                    break;
                case EXPIRED:
                    processExpiredCoupons(couponInfo, status);
                    break;
            }
        }
    }

    // Wrap one more layer for extensibility
    private void processUsedCoupons(CouponKafkaMessage kafkaMessage,
                                    CouponStatus status){
        processCouponByStatus(kafkaMessage, status);
    }

    private void processExpiredCoupons(CouponKafkaMessage kafkaMessage,
                                    CouponStatus status){
        processCouponByStatus(kafkaMessage, status);
    }

    private void processCouponByStatus(CouponKafkaMessage kafkaMessage,
                                       CouponStatus status){
        List<Coupon> coupons = couponDao.findAllById(kafkaMessage.getIds());
        if (CollectionUtils.isEmpty(coupons) || coupons.size() != kafkaMessage.getIds().size()) {
            log.error("Cannot find right coupon info: {}", JSON.toJSONString(kafkaMessage));
            return;
        }
        coupons.forEach(c -> {
            c.setStatus(status);
        });
        log.info("Processed messages count: {}", couponDao.saveAll(coupons).size());
    }
}
