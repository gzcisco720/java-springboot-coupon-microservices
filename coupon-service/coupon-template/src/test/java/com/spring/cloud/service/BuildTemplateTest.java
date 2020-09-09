package com.spring.cloud.service;

import com.alibaba.fastjson.JSON;
import com.spring.cloud.constant.CouponCategory;
import com.spring.cloud.constant.DistributeTarget;
import com.spring.cloud.constant.PeriodType;
import com.spring.cloud.constant.ProductLine;
import com.spring.cloud.dto.TemplateRequest;
import com.spring.cloud.vo.TemplateRule;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

@SpringBootTest
@RunWith(SpringRunner.class)
public class BuildTemplateTest {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void testBuildTemplate() throws Exception {

        System.out.println(JSON.toJSONString(
                buildTemplateService.buildTemplate(fakeTemplateRequest())
        ));
        Thread.sleep(5000);
    }

    private TemplateRequest fakeTemplateRequest() {

        TemplateRequest request = new TemplateRequest();
        request.setName("coupon_template-" + new Date().getTime());
        request.setLogo("abc");
        request.setDesc("This is a coupon template");
        request.setCategory(CouponCategory.DEDUCTION.getCode());
        request.setProductLine(ProductLine.ONE.getCode());
        request.setCount(10000);
        request.setUserId(10001L);
        request.setTarget(DistributeTarget.SINGLE.getCode());

        TemplateRule rule = new TemplateRule();
        rule.setExpiration(new TemplateRule.Expiration(
                PeriodType.SHIFT.getCode(),
                1, DateUtils.addDays(new Date(), 60).getTime()
        ));
        rule.setDiscount(new TemplateRule.Discount(5, 1));
        rule.setLimitation(1);
        rule.setUsage(new TemplateRule.Usage(
                "VIC", "Melbourne",
                JSON.toJSONString(Arrays.asList("Entertainment", "Home"))
        ));
        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));

        request.setRule(rule);

        return request;
    }
}
