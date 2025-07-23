package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import com.sist.web.model.Order;
import com.sist.web.model.OrderDetail;

public interface OrderDao {
    
    // 주문 삽입
    void insertOrder(Order order);
    
    // 주문 상세 삽입
    void insertOrderDetail(OrderDetail orderDetail);

    // 단건 주문 조회
    Order selectOrderById(String orderId);

    // 해당 주문의 상세 목록 조회
    List<OrderDetail> selectOrderDetailsByOrderId(String orderId);
    
    // 주문의 refund 여부 업데이트
    void updateOrderRefund(Map<String, Object> param);
    
    // 주문상태(결제시) 업데이트
    void updateOrderStatus(Map<String, Object> param);
    
    // 자동삭제 스케줄러 관련
    List<String> selectExpiredOrderIds(Map<String, Object> param);
    void deleteOrderById(String orderId);
    void deleteOrderDetailsByOrderId(String orderId);
    
    // 유저 주문내역
    public List<Order> userOrderlist(String userId);
    
    // 유저 주문상세
    public OrderDetail userOrderDetails(String orderId);
    
    // 체크인 날짜 하루 전인지
    public int userOrderCheckin(String orderId);
    
    // 체크아웃 날짜 한달 내인지
    public int userOrderCheckout(String orderId);
    
    // 판매자 페이지 정산 내역 띄워주는 거
    public List<OrderDetail> sellerSellList(String sellerId);
    
    // 판매자 정산 총액 띄워주는 거
    public int sellerSum(String sellerId);
    
}
