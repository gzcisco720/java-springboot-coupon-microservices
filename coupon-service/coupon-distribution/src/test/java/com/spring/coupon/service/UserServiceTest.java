package com.spring.coupon.service;

import com.alibaba.fastjson.JSON;
import com.spring.coupon.constant.CouponStatus;
import com.spring.coupon.exception.CouponException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    private Long mockUserId = 20001L;

    @Autowired
    private IUserService userService;

    @Test
    public void testFindCouponByStatus() throws CouponException {
        System.out.println(
                JSON.toJSONString(userService.findCouponsByStatus(mockUserId, CouponStatus.USABLE.getCode()))
        );
    }

    @Test
    public void testAvailableTemplate() throws CouponException {
        System.out.println(
                JSON.toJSONString(userService.findAvailableTemplate(mockUserId))
        );
    }
}
