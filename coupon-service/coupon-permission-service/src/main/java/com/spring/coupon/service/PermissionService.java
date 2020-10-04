package com.spring.coupon.service;

import com.spring.coupon.constant.UserRole;
import com.spring.coupon.dao.PathRepository;
import com.spring.coupon.dao.RolePathMappingRepository;
import com.spring.coupon.dao.RoleRepository;
import com.spring.coupon.dao.UserRoleMappingRepository;
import com.spring.coupon.entity.Path;
import com.spring.coupon.entity.Role;
import com.spring.coupon.entity.RolePathMapping;
import com.spring.coupon.entity.UserRoleMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class PermissionService {
    private final PathRepository pathRepository;
    private final RoleRepository roleRepository;
    private final UserRoleMappingRepository userRoleMappingRepository;
    private final RolePathMappingRepository rolePathMappingRepository;

    public PermissionService(PathRepository pathRepository,
                             RoleRepository roleRepository,
                             UserRoleMappingRepository userRoleMappingRepository,
                             RolePathMappingRepository rolePathMappingRepository) {
        this.pathRepository = pathRepository;
        this.roleRepository = roleRepository;
        this.userRoleMappingRepository = userRoleMappingRepository;
        this.rolePathMappingRepository = rolePathMappingRepository;
    }

    public Boolean checkPermission(Long userId, String uri, String httpMethod) {
        UserRoleMapping userRoleMapping = userRoleMappingRepository.findByUserId(userId);
        if (null == userRoleMapping) {
            log.error("userId do not have mapping: {}", userId);
            return false;
        }
        Optional<Role> role = roleRepository.findById(userRoleMapping.getRoleId());
        if (!role.isPresent()) {
            log.error("roleId do not have mapping: {}", userRoleMapping.getRoleId());
            return false;
        }
        if (role.get().getRoleTag().equals(UserRole.SUPER_ADMIN.name())) {
            return true;
        }
        Path path = pathRepository.findByPathPatternAndHttpMethod(uri, httpMethod);
        if (null == path) {
            return true;
        }
        RolePathMapping rolePathMapping = rolePathMappingRepository
                .findByRoleIdAndPathId(role.get().getId(), path.getId());
        return rolePathMapping != null;
    }
}
