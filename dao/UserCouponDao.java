package com.sist.web.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.model.UserCoupon;

@Repository("userCouponDao")
public interface UserCouponDao {

    // 유저가 특정 쿠폰을 발급받았는지 확인
    public int hasCoupon(String userId, String couponId);

    // 쿠폰 발급
    public int insertUserCoupon(UserCoupon userCoupon);

    // 마이페이지: 내가 발급받은 쿠폰 목록
    public List<UserCoupon> selectUserCouponList(String userId);
    
   
    public int existsUserCoupon(@Param("userId") String userId, @Param("couponId") String couponId);

    // 특정 쿠폰 상세 조회
    public UserCoupon selectUserCouponById(String userCouponId);

    // 쿠폰 사용 처리
    public int updateUserCouponUse(String userCouponId);
    
    public int updateUserCouponRefund(String userCouponId);
    // 기본 쿠폰 쿼리 조인해서 조회
    public List<UserCoupon> selectUserCouponsByUserId(String userId);
    
    
}