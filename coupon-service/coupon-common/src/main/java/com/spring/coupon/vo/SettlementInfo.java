package com.spring.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementInfo {

    private Long userId;
    private List<CouponAndTemplateInfo> couponAndTemplateInfos;
    private List<GoodsInfo> goodsInfos;
    private Boolean employ;
    private Double cost;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CouponAndTemplateInfo {
        // Coupon primary key
        private Integer id;
        private CouponTemplateSDK template;
    }
}
