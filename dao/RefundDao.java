package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import com.sist.web.model.Refund;

public interface RefundDao {
    // 1. 환불 정보 삽입
    void insertRefund(Refund refund);

    // 2. 주문 ID로 환불 내역 조회 (1:N 가능성 대비 리스트로 반환)
    List<Refund> getRefundByOrderId(String orderId);

    // 3. 환불 상태 업데이트 (REFUND_RESULT 변경)
    void updateRefundResult(Map<String, Object> param); 
    
    //REFUND 관련 시퀀스 만들었음
    
    String getRefundSeq();
}
