package com.spring.coupon.dao;

import com.spring.coupon.entity.RolePathMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePathMappingRepository extends JpaRepository<RolePathMapping, Integer> {
    RolePathMapping findByRoleIdAndPathId(Integer roleId, Integer pathId);
}
