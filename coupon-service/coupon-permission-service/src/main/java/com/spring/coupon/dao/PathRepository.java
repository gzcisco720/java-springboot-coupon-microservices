package com.spring.coupon.dao;

import com.spring.coupon.entity.Path;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PathRepository extends JpaRepository<Path, Integer> {
    List<Path> findAllByServiceName(String serviceName);
    Path findByPathPatternAndHttpMethod(String pathPattern, String httpMethod);

}
