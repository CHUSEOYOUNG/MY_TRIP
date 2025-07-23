package com.sist.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.CouponDao;
import com.sist.web.dao.UserCouponDao;
import com.sist.web.model.Coupon;
import com.sist.web.model.UserCoupon;

@Service("userCouponService")
public class UserCouponService {
	
	@Autowired
	private CouponDao couponDao;
	
    @Autowired
    private UserCouponDao userCouponDao;
    
    //유저쿠폰ID에서 쿠폰ID 추출
    public String getCouponId(String userCouponId) {
    	return userCouponDao.selectUserCouponById(userCouponId).getCouponId();
    }
    // 단순 쿠폰 조회용도(사용자용)
    public Coupon selectCoupon(String userCouponId) {
    	String couponId = getCouponId(userCouponId);
    	return couponDao.selectCouponById(couponId);
    }
    
    // ✅ 특정 유저가 특정 쿠폰을 발급받았는지 확인
    public boolean hasCoupon(String userId, String couponId) {
        return userCouponDao.existsUserCoupon(userId, couponId) > 0;
    }

    // ✅ 유저 쿠폰 발급
    public boolean insertUserCoupon(UserCoupon userCoupon) {
        return userCouponDao.insertUserCoupon(userCoupon) > 0;
    }

    // ✅ 유저가 가진 모든 쿠폰 리스트 조회 (쿼리조인)
    public List<UserCoupon> getUserCouponList(String userId) {
        return userCouponDao.selectUserCouponsByUserId(userId);
    }

    // ✅ 특정 유저 쿠폰 상세 조회
    public UserCoupon getUserCouponById(String userCouponId) {
        return userCouponDao.selectUserCouponById(userCouponId);
    }

    // ✅ 쿠폰 사용 처리
    public boolean useCoupon(String userCouponId) {
        return userCouponDao.updateUserCouponUse(userCouponId) > 0;
    }
    
    // (쿠폰 금액, (본)쿠폰과 매칭해서 가져오기)
    public int calculateDiscount(int originalAmount, Coupon coupon) {
        if (coupon == null) return 0;

        if ("AMOUNT".equalsIgnoreCase(coupon.getCouponType())) {
            return coupon.getCouponAmount();
        } else if ("PERCENT".equalsIgnoreCase(coupon.getCouponType())) {
            int discount = (int) Math.floor(originalAmount * (coupon.getCouponAmount() / 100.0));
            return Math.min(discount, coupon.getCouponMaxAmount()); // 최대할인 적용
        }
        return 0;
    }
}