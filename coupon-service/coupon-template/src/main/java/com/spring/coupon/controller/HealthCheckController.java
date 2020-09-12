package com.spring.coupon.controller;

import com.spring.coupon.exception.CouponException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class HealthCheckController {

    private final DiscoveryClient discoveryClient;
    private final Registration registration;

    public HealthCheckController(DiscoveryClient discoveryClient, @Qualifier("eurekaRegistration") Registration registration) {
        this.discoveryClient = discoveryClient;
        this.registration = registration;
    }

    @GetMapping("/health")
    public String health() {
        log.debug("view health api");
        return "CouponTemplate is alive";
    }

    @GetMapping("/exception")
    public String exception() throws CouponException {
        log.debug("view exception api");
        throw new CouponException("CouponTemplate got problem");
    }

    @GetMapping("/info")
    public List<Map<String,Object>> info() {
        List<ServiceInstance> instances =
                discoveryClient.getInstances(registration.getServiceId());
        List<Map<String,Object>> result =
                new ArrayList<>(instances.size());
        instances.forEach(i -> {
            Map<String, Object> info = new HashMap<>();
            info.put("serviceId", i.getServiceId());
            info.put("instanceId", i.getInstanceId());
            info.put("port", i.getPort());
            result.add(info);
        });
        return result;
    }
}
