package com.spring.coupon;

import com.spring.coupon.permission.PermissionClient;
import com.spring.coupon.vo.PermissionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;

@Slf4j
@Component
public class PermissionDetectListener implements
        ApplicationListener<ApplicationReadyEvent> {
    private static final String KEY_SERVER_CTX = "server.servlet.context-path";
    private static final String KEY_SERVICE_NAME = "spring.application.name";

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        ApplicationContext ctx = event.getApplicationContext();
        new Thread(() -> {
            List<PermissionInfo> infoList = scanPermission(ctx);
            registerPermission(infoList, ctx);
        }).start();
    }

    @SuppressWarnings("all")
    private void registerPermission(List<PermissionInfo> infoList, ApplicationContext ctx) {
        log.info("================================================");
        PermissionClient permissionClient = ctx.getBean(PermissionClient.class);
        if (null == permissionClient) {
            log.error("no permission client");
            return;
        }
        String serviceName = ctx.getEnvironment().getProperty(KEY_SERVICE_NAME);
        log.info("service name: {}", serviceName);
        boolean result = new PermissionRegistry(permissionClient, serviceName).register(infoList);
        if (result) {
            log.info("register successfully");
        }
    }

    private List<PermissionInfo> scanPermission(ApplicationContext ctx) {
        String pathPrefix = ctx.getEnvironment().getProperty(KEY_SERVER_CTX);
        RequestMappingHandlerMapping mapping =
                (RequestMappingHandlerMapping) ctx.getBean("requestMappingHandlerMapping");
        List<PermissionInfo> permissionInfoList = new AnnotationScanner(pathPrefix).scanPermission(
                mapping.getHandlerMethods()
        );
        permissionInfoList.forEach(p -> log.info("{}", p));
        log.info("{} permission found", permissionInfoList.size());
        log.info("================================================");
        return permissionInfoList;
    }

}
