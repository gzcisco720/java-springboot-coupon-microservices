package com.spring.coupon.executer;

import com.alibaba.fastjson.JSON;
import com.spring.coupon.vo.GoodsInfo;
import com.spring.coupon.vo.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractExecutor {
    @SuppressWarnings("all")
    protected boolean isGoodsTypeOkay(SettlementInfo settlement){
        List<Integer> goodsType = settlement.getGoodsInfos()
                .stream().map(GoodsInfo::getType)
                .collect(Collectors.toList());

        List<Integer> templateGoodsType = JSON.parseObject(
                settlement.getCouponAndTemplateInfos().get(0).getTemplate().getRule().getUsage().getGoodsType(),
                List.class
        );

        return CollectionUtils.isNotEmpty(
                CollectionUtils.intersection(goodsType, templateGoodsType)
        );
    }

    protected SettlementInfo processGoodsTypeNotSatisfy(
            SettlementInfo settlementInfo, double cost
    ) {
        boolean isGoodsTypeOkay = isGoodsTypeOkay(settlementInfo);
        if (!isGoodsTypeOkay) {
           settlementInfo.setCost(cost);
           settlementInfo.setCouponAndTemplateInfos(Collections.emptyList());
           return settlementInfo;
        }
        return null;
    }

    protected double calculate(List<GoodsInfo> goodsInfos) {
        return goodsInfos.stream().mapToDouble(
                g -> g.getPrice() * g.getCount()
        ).sum();
    }

    protected double twoDecimals (double val) {
        return  new BigDecimal(val).setScale(
                2, BigDecimal.ROUND_HALF_UP
        ).doubleValue();
    }

    protected double minCost() {
        return 0.1;
    }
}
