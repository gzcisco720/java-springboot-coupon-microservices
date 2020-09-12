package com.spring.coupon.filter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Component
public class RateLimitFilter extends AbstractPreZuulFilter {

    RateLimiter rateLimiter = RateLimiter.create(2.0);

    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        if(rateLimiter.tryAcquire()){
            log.info("get rate token success");
            return success();
        } else {
            log.error("rate limit {}", request.getRequestURI());
            return fail(402, "Error: rate limit");
        }
    }

    @Override
    public int filterOrder() {
        return 2;
    }
}
