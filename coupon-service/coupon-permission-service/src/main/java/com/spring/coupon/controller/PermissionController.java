package com.spring.coupon.controller;

import com.spring.coupon.annotation.IgnoreCommonResponse;
import com.spring.coupon.service.PathService;
import com.spring.coupon.service.PermissionService;
import com.spring.coupon.vo.CheckPermissionRequest;
import com.spring.coupon.vo.CreatePathRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class PermissionController {

    private final PathService pathService;
    private final PermissionService permissionService;

    public PermissionController(PathService pathService, PermissionService permissionService) {
        this.pathService = pathService;
        this.permissionService = permissionService;
    }

    @PostMapping("/create/path")
    public List<Integer> createPath(@RequestBody CreatePathRequest request) {
        log.info("createPath: {}", request.getPathInfos().size());
        return pathService.createPath(request);
    }

    @IgnoreCommonResponse
    @PostMapping("/check/permission")
    public Boolean checkPermission(@RequestBody CheckPermissionRequest request) {
        log.info("checkPermission: {}, {}, {}", request.getUri(), request.getHttpMethod(), request.getUserId());
        return  permissionService.checkPermission(request.getUserId(), request.getUri(), request.getHttpMethod());
    }
}
