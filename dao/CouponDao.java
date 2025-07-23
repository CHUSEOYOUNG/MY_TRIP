package com.sist.web.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.sist.web.model.Coupon;

@Repository("couponDao")
public interface CouponDao {

	
    // 쿠폰 ID로 쿠폰 조회
    public Coupon selectCouponById(String couponId);

    // 쿠폰 전체 목록 조회 (관리자용 등)
    public List<Coupon> selectCouponList();

    // 쿠폰 등록
    public int insertCoupon(Coupon coupon);

    // 쿠폰 수량 감소
    public int decreaseCouponCount(String couponId);

    // 쿠폰 수량 증가 (취소 시 등)
    public int increaseCouponCount(String couponId);
	
}
