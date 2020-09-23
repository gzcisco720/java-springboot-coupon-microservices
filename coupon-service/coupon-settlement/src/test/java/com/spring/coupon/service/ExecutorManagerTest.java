package com.spring.coupon.service;

import com.alibaba.fastjson.JSON;
import com.spring.coupon.constant.CouponCategory;
import com.spring.coupon.constant.GoodsType;
import com.spring.coupon.exception.CouponException;
import com.spring.coupon.executer.ExecutorManager;
import com.spring.coupon.vo.CouponTemplateSDK;
import com.spring.coupon.vo.GoodsInfo;
import com.spring.coupon.vo.SettlementInfo;
import com.spring.coupon.vo.TemplateRule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class ExecutorManagerTest {
    private Long mockUserId = 20001L;

    @Autowired
    private ExecutorManager manager;

    @Test
    public void testComputeRule() throws CouponException {
        log.info("Rebate coupon executor test");
        SettlementInfo rebateCoupon = mockRebateCoupon();
        SettlementInfo rebateResult = manager.computeRule(rebateCoupon);
        log.info("{}", rebateResult.getCost());
        log.info("{}", rebateResult.getCouponAndTemplateInfos().size());
        log.info("{}", rebateResult.getCouponAndTemplateInfos());

        log.info("Discount coupon executor test");
        SettlementInfo discountCoupon = mockDiscountCoupon();
        SettlementInfo discountResult = manager.computeRule(discountCoupon);
        log.info("{}", discountResult.getCost());
        log.info("{}", discountResult.getCouponAndTemplateInfos().size());
        log.info("{}", discountResult.getCouponAndTemplateInfos());

        log.info("Deduction coupon executor test");
        SettlementInfo deductionCoupon = mockDeductionCoupon();
        SettlementInfo deductionResult = manager.computeRule(deductionCoupon);
        log.info("{}", deductionResult.getCost());
        log.info("{}", deductionResult.getCouponAndTemplateInfos().size());
        log.info("{}", deductionResult.getCouponAndTemplateInfos());

        log.info("Rebate_Discount coupon executor test");
        SettlementInfo rebateDiscountCoupon = mockRebateDiscountCoupon();
        SettlementInfo rebateDiscountResult = manager.computeRule(rebateDiscountCoupon);
        log.info("{}", rebateDiscountResult.getCost());
        log.info("{}", rebateDiscountResult.getCouponAndTemplateInfos().size());
        log.info("{}", rebateDiscountResult.getCouponAndTemplateInfos());
    }

    private SettlementInfo mockRebateCoupon() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(mockUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo1 = new GoodsInfo();
        goodsInfo1.setCount(2);
        goodsInfo1.setPrice(10.88);
        goodsInfo1.setType(GoodsType.ENTERTAINMENT.getCode());

        GoodsInfo goodsInfo2 = new GoodsInfo();
        goodsInfo2.setCount(10);
        goodsInfo2.setPrice(20.88);
        goodsInfo2.setType(GoodsType.ENTERTAINMENT.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo1,goodsInfo2));
        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(1);
        templateSDK.setCategory(CouponCategory.DEDUCTION.getCode());
        templateSDK.setKey("100120190801");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(20, 199));
        rule.setUsage(new TemplateRule.Usage(
        "Vic", "Mel",
            JSON.toJSONString(Arrays.asList(GoodsType.ENTERTAINMENT.getCode(), GoodsType.FOOD.getCode()))
        ));

        templateSDK.setRule(rule);
        ctInfo.setTemplate(templateSDK);
        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }

    private SettlementInfo mockDiscountCoupon() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(mockUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo1 = new GoodsInfo();
        goodsInfo1.setCount(2);
        goodsInfo1.setPrice(10.88);
        goodsInfo1.setType(GoodsType.ENTERTAINMENT.getCode());

        GoodsInfo goodsInfo2 = new GoodsInfo();
        goodsInfo2.setCount(10);
        goodsInfo2.setPrice(20.88);
        goodsInfo2.setType(GoodsType.ENTERTAINMENT.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo1,goodsInfo2));
        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(1);
        templateSDK.setCategory(CouponCategory.DISCOUNT.getCode());
        templateSDK.setKey("100120190801");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(85, 1));
        rule.setUsage(new TemplateRule.Usage(
                "Vic", "Mel",
                JSON.toJSONString(Arrays.asList(GoodsType.ENTERTAINMENT.getCode(), GoodsType.FOOD.getCode()))
        ));
        templateSDK.setRule(rule);
        ctInfo.setTemplate(templateSDK);
        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }

    private SettlementInfo mockDeductionCoupon() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(mockUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo1 = new GoodsInfo();
        goodsInfo1.setCount(2);
        goodsInfo1.setPrice(10.88);
        goodsInfo1.setType(GoodsType.ENTERTAINMENT.getCode());

        GoodsInfo goodsInfo2 = new GoodsInfo();
        goodsInfo2.setCount(10);
        goodsInfo2.setPrice(20.88);
        goodsInfo2.setType(GoodsType.ENTERTAINMENT.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo1,goodsInfo2));

        SettlementInfo.CouponAndTemplateInfo ctInfo = new SettlementInfo.CouponAndTemplateInfo();
        ctInfo.setId(1);

        CouponTemplateSDK templateSDK = new CouponTemplateSDK();
        templateSDK.setId(1);
        templateSDK.setCategory(CouponCategory.DEDUCTION.getCode());
        templateSDK.setKey("100120190801");

        TemplateRule rule = new TemplateRule();
        rule.setDiscount(new TemplateRule.Discount(5, 1));
        rule.setUsage(new TemplateRule.Usage(
                "Vic", "Mel",
                JSON.toJSONString(Arrays.asList(GoodsType.ENTERTAINMENT.getCode(), GoodsType.FOOD.getCode()))
        ));

        templateSDK.setRule(rule);
        ctInfo.setTemplate(templateSDK);
        info.setCouponAndTemplateInfos(Collections.singletonList(ctInfo));

        return info;
    }

    private SettlementInfo mockRebateDiscountCoupon() {
        SettlementInfo info = new SettlementInfo();
        info.setUserId(mockUserId);
        info.setEmploy(false);
        info.setCost(0.0);

        GoodsInfo goodsInfo1 = new GoodsInfo();
        goodsInfo1.setCount(2);
        goodsInfo1.setPrice(10.88);
        goodsInfo1.setType(GoodsType.ENTERTAINMENT.getCode());

        GoodsInfo goodsInfo2 = new GoodsInfo();
        goodsInfo2.setCount(10);
        goodsInfo2.setPrice(20.88);
        goodsInfo2.setType(GoodsType.ENTERTAINMENT.getCode());

        info.setGoodsInfos(Arrays.asList(goodsInfo1,goodsInfo2));

        SettlementInfo.CouponAndTemplateInfo rebateInfo = new SettlementInfo.CouponAndTemplateInfo();
        rebateInfo.setId(1);

        CouponTemplateSDK rebateTemplate = new CouponTemplateSDK();
        rebateTemplate.setId(1);
        rebateTemplate.setCategory(CouponCategory.REBATE.getCode());
        rebateTemplate.setKey("100120190712");

        TemplateRule rebateRule = new TemplateRule();
        rebateRule.setDiscount(new TemplateRule.Discount(20, 199));
        rebateRule.setUsage(new TemplateRule.Usage(
                "Vic", "Mel",
                JSON.toJSONString(Arrays.asList(GoodsType.ENTERTAINMENT.getCode(), GoodsType.FOOD.getCode()))
        ));
        rebateRule.setWeight(JSON.toJSONString(Collections.emptyList()));
        rebateTemplate.setRule(rebateRule);
        rebateInfo.setTemplate(rebateTemplate);

        SettlementInfo.CouponAndTemplateInfo discountInfo = new SettlementInfo.CouponAndTemplateInfo();
        discountInfo.setId(1);

        CouponTemplateSDK discountTemplate = new CouponTemplateSDK();
        discountTemplate.setId(1);
        discountTemplate.setCategory(CouponCategory.DISCOUNT.getCode());
        discountTemplate.setKey("100220190712");

        TemplateRule discountRule = new TemplateRule();
        discountRule.setDiscount(new TemplateRule.Discount(85, 1));
        discountRule.setUsage(new TemplateRule.Usage(
                "Vic", "Mel",
                JSON.toJSONString(Arrays.asList(GoodsType.ENTERTAINMENT.getCode(), GoodsType.FOOD.getCode()))
        ));
        discountRule.setWeight(JSON.toJSONString(
                Collections.singletonList("1001201907120001")
        ));

        discountTemplate.setRule(discountRule);
        discountInfo.setTemplate(discountTemplate);
        info.setCouponAndTemplateInfos(Arrays.asList(
                rebateInfo, discountInfo
        ));
        return info;
    }
}
