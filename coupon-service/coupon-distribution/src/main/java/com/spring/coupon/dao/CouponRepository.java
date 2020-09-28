package com.spring.coupon.dao;

import com.spring.coupon.constant.CouponStatus;
import com.spring.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}
