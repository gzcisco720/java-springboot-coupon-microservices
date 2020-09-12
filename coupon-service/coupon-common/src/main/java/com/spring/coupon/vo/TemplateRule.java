package com.spring.coupon.vo;

import com.spring.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRule {

    private Expiration expiration;
    private Discount discount;
    private Integer limitation;
    private Usage usage;
    private String weight;

    public boolean validate() {
        return expiration.validate() && discount.validate()
                && limitation > 0 && usage.validate()
                && StringUtils.isNotEmpty(weight);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Expiration {
        private Integer period;
        private Integer gap;
        private Long deadline;

        boolean validate() {
            return null!= PeriodType.of(period) && gap>0 && deadline>0;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Discount {
        private Integer quota;
        private Integer base;

        boolean validate() {
            return quota>0 && base >0;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage {
        private String province;
        private String city;
        private String goodsType;

        boolean validate() {
            return StringUtils.isNotBlank(province)
                    && StringUtils.isNotBlank(city)
                    && StringUtils.isNotBlank(goodsType);
        }
    }
}
