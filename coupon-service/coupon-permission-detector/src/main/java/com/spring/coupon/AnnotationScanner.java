package com.spring.coupon;

import com.spring.coupon.annotation.CouponPermission;
import com.spring.coupon.annotation.IgnoreCouponPermission;
import com.spring.coupon.vo.PermissionInfo;
import com.spring.coupon.constant.HttpMethodEnum;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class AnnotationScanner {

    private String pathPrefix;
    private static final String COUPON_PKG = "com.spring.coupon";

    public AnnotationScanner(String pathPrefix) {
        this.pathPrefix = trimPath(pathPrefix);
    }

    List<PermissionInfo> scanPermission(
            Map<RequestMappingInfo, HandlerMethod> mappingMap
    ) {
        List<PermissionInfo> result = new ArrayList<>();
        mappingMap.forEach((info, method) -> result.addAll(buildPermission(info, method)));
        return result;
    }

    private List<PermissionInfo> buildPermission (
            RequestMappingInfo mappingInfo, HandlerMethod handlerMethod
    ) {
        Method javaMethod = handlerMethod.getMethod();
        Class baseClass = javaMethod.getDeclaringClass();
        if (!isCouponPackage(baseClass.getName())) {
            log.debug("ignore method: {}", javaMethod.getName());
            return Collections.emptyList();
        }
        IgnoreCouponPermission ignoreCouponPermission = javaMethod.getAnnotation(IgnoreCouponPermission.class);
        if(null != ignoreCouponPermission) {
            log.debug("ignore method: {}", javaMethod.getName());
            return Collections.emptyList();
        }
        CouponPermission couponPermission = javaMethod.getAnnotation(
          CouponPermission.class
        );
        if(null != couponPermission) {
            log.debug("ignore @CouponPermission: {}, {}",
                    javaMethod.getDeclaringClass().getName(),
                    javaMethod.getName());
            return Collections.emptyList();
        }
        Set<String> urlSet = mappingInfo.getPatternsCondition().getPatterns();
        boolean isAllMethod = false;
        Set<RequestMethod> methodSet = mappingInfo.getMethodsCondition().getMethods();
        if(CollectionUtils.isEmpty(methodSet)) {
            isAllMethod = true;
        }
        List<PermissionInfo> infoList = new ArrayList<>();
        for (String url : urlSet) {
            if (isAllMethod) {
                PermissionInfo info = buildPermissionInfo(
                        HttpMethodEnum.ALL.name(),
                        javaMethod.getName(),
                        this.pathPrefix+url,
                        couponPermission.readOnly(),
                        couponPermission.description(),
                        couponPermission.extra()
                );
                infoList.add(info);
                continue;
            }
            for (RequestMethod method : methodSet) {
                PermissionInfo info = buildPermissionInfo(
                        method.name(),
                        javaMethod.getName(),
                        this.pathPrefix + url,
                        couponPermission.readOnly(),
                        couponPermission.description(),
                        couponPermission.extra()
                );
                infoList.add(info);
                log.info("permission detected: {}", info);
            }
        }
        return infoList;
    }
    private PermissionInfo buildPermissionInfo(
            String reqMethod, String javaMethod, String path,
            boolean readOnly, String desp, String extra
    ) {
        PermissionInfo info = new PermissionInfo();
        info.setMethod(reqMethod);
        info.setUrl(path);
        info.setIsRead(readOnly);
        info.setDescription(
                StringUtils.isAllEmpty(desp)?javaMethod:desp
        );
        info.setExtra(extra);
        return info;
    }
    private boolean isCouponPackage(String className) {
        return className.startsWith(COUPON_PKG);
    }
    private String trimPath(String path) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length()-1);
        }
        return path;
    }
}
