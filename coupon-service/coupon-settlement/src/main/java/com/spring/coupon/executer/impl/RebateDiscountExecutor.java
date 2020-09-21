package com.spring.coupon.executer.impl;

import com.alibaba.fastjson.JSON;
import com.spring.coupon.constant.CouponCategory;
import com.spring.coupon.constant.RuleFlag;
import com.spring.coupon.executer.AbstractExecutor;
import com.spring.coupon.executer.IRuleExecutor;
import com.spring.coupon.vo.GoodsInfo;
import com.spring.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RebateDiscountExecutor extends AbstractExecutor implements IRuleExecutor {
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.REBATE_DISCOUNT;
    }

    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double sum = twoDecimals(
                calculate(settlement.getGoodsInfos())
        );
        SettlementInfo probability = processGoodsTypeNotSatisfy(
                settlement, sum
        );
        if (null != probability) {
            log.debug("Rebate_Discount template is not match goods type");
            return probability;
        }
        SettlementInfo.CouponAndTemplateInfo rebate = null;
        SettlementInfo.CouponAndTemplateInfo discount = null;

        for (SettlementInfo.CouponAndTemplateInfo ct : settlement.getCouponAndTemplateInfos()) {
            if(CouponCategory.of(ct.getTemplate().getCategory())==CouponCategory.REBATE) {
                rebate = ct;
            } else {
                discount = ct;
            }
        }
        assert null != rebate;
        assert null != discount;

        if(!canTemplateUseTogether(rebate, discount)) {
            settlement.setCost(sum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        double rebateBase = (double) rebate.getTemplate().getRule().getDiscount().getBase();
        double rebateQuota = (double) rebate.getTemplate().getRule().getDiscount().getQuota();

        double finalSum = sum;

        if (finalSum >= rebateBase) {
          finalSum -= rebateQuota;
          ctInfos.add(rebate);
        }

        double rebateDiscount = (double) discount.getTemplate().getRule().getDiscount().getQuota();
        finalSum *= rebateDiscount * 1.0 / 100 ;
        ctInfos.add(discount);

        settlement.setCouponAndTemplateInfos(ctInfos);
        settlement.setCost(twoDecimals(
                Math.max(finalSum, minCost())
        ));
        log.debug("Use rebate_discount coupon");
        return settlement;
    }

    @Override
    @SuppressWarnings("all")
    protected boolean isGoodsTypeOkay(SettlementInfo settlement) {
        log.debug("Check rebate and discount");
        List<Integer> goodsType = settlement.getGoodsInfos().stream()
                .map(GoodsInfo::getType).collect(Collectors.toList());
        List<Integer> templateGoodsType = new ArrayList<>();

        settlement.getCouponAndTemplateInfos().forEach(ct -> {
            templateGoodsType.addAll(JSON.parseObject(
                    ct.getTemplate().getRule().getUsage().getGoodsType(),
                    List.class
            ));
        });

        return CollectionUtils.isEmpty(CollectionUtils.subtract(
                goodsType, templateGoodsType
        ));
    }

    @SuppressWarnings("all")
    private boolean canTemplateUseTogether(
            SettlementInfo.CouponAndTemplateInfo rebate,
            SettlementInfo.CouponAndTemplateInfo discount
    ) {
        String rebateKey = rebate.getTemplate().getKey()+String.format("%04d", rebate.getTemplate().getId());
        String discountKey = discount.getTemplate().getKey()+String.format("%04d", rebate.getTemplate().getId());
        List<String> allSharedKeysForRebate = new ArrayList<>();
        allSharedKeysForRebate.add(rebateKey);
        allSharedKeysForRebate.addAll(JSON.parseObject(
                rebate.getTemplate().getRule().getWeight(),
                List.class
        ));
        List<String> allSharedKeysForDiscount = new ArrayList<>();
        allSharedKeysForDiscount.add(discountKey);
        allSharedKeysForDiscount.addAll(JSON.parseObject(
                discount.getTemplate().getRule().getWeight(),
                List.class
        ));

        return CollectionUtils.isSubCollection(Arrays.asList(rebateKey, discountKey), allSharedKeysForRebate)||
                CollectionUtils.isSubCollection(Arrays.asList(rebateKey, discountKey), allSharedKeysForDiscount);
    }
}
