package com.sist.web.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.dao.AccommodationRoomDao;
import com.sist.web.dao.CouponDao;
import com.sist.web.dao.OrderDao;
import com.sist.web.dao.RefundDao;
import com.sist.web.dao.UserCouponDao;
import com.sist.web.model.Order;
import com.sist.web.model.OrderDetail;
import com.sist.web.model.Refund;
import com.sist.web.model.UserCoupon;

@Service
public class OrderService {
	private static Logger logger = LoggerFactory.getLogger(EditorService.class);
	
	@Autowired
	private RefundDao refundDao;
	
    @Autowired
    private OrderDao orderDao;
    
    @Autowired
    private AccommodationRoomDao accommodationRoomDao;
    
    @Autowired
    private UserCouponDao userCouponDao;
    
    // 단건 주문 등록
    public void createOrder(String userId, String roomId, LocalDate checkIn, LocalDate checkOut,
                            int totalAmount, String couponId, String paymentMethod) {

        String orderId = UUID.randomUUID().toString(); // ORDER_ID 생성
        String orderDetailId = UUID.randomUUID().toString(); // 상세 ID도 생성

        // 👉 주문 정보 생성
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setOrderTotalAmount(totalAmount);
        order.setOrderStatus("W"); // 예: W = 결제대기
        order.setOrderDate(java.sql.Date.valueOf(LocalDate.now()));
        order.setOrderCouponId(couponId); // null 허용
        
        orderDao.insertOrder(order);

        // 👉 상세 주문 정보 생성
        OrderDetail detail = new OrderDetail();
        detail.setOrderDetailsId(orderDetailId);
        detail.setOrderId(orderId);
        detail.setAccommRoomId(roomId);
        detail.setOrderDetailsCheckinDate(java.sql.Date.valueOf(checkIn));
        detail.setOrderDetailsCheckoutDate(java.sql.Date.valueOf(checkOut));
        detail.setOrderDetailsRoomEachPrice(totalAmount);
        detail.setOrderDetailsCount(1); // 기본 1건
        detail.setOrderDetailsPaymentMethod(paymentMethod);
        detail.setOrderDetailsPeopleCount(2); // 기본값, 또는 인자로 받을 수 있음

        orderDao.insertOrderDetail(detail);
    }
    
    @Transactional
    public boolean insertOrder(Order order, OrderDetail detail, String checkIn, String checkOut) {
        Map<String, Object> param = new HashMap<>();
        param.put("roomId", detail.getAccommRoomId());
        param.put("checkIn", checkIn);
        param.put("checkOut", checkOut);

        int overbookedDays = accommodationRoomDao.isRoomOverbooked(param);
        if (overbookedDays > 0) {
            return false; // 이미 예약 불가한 날짜가 있음
        }

        orderDao.insertOrder(order);
        orderDao.insertOrderDetail(detail);

        if (order.getOrderCouponId() != null) {
            userCouponDao.updateUserCouponUse(order.getOrderCouponId());
        }

        return true;
    }
    
    @Autowired
    private AccommodationRoomPriceService accommodationRoomPriceService;

    @Autowired
    private UserCouponService userCouponService;

    public int calculateTotalPrice(String roomId, String checkInStr, String checkOutStr, String couponId) {
        int basePrice = (int) accommodationRoomPriceService.calculateTotalPrice(roomId, checkInStr, checkOutStr).getTotalPrice();
        int discount = 0;

        if (couponId != null && !couponId.isEmpty()) {
            UserCoupon coupon = userCouponService.getUserCouponById(couponId);
            if (coupon != null) {
                if ("P".equals(coupon.getCouponType())) {
                    discount = (int) (basePrice * (coupon.getCouponAmount() / 100.0));
                    if (coupon.getCouponMaxAmount() > 0 && discount > coupon.getCouponMaxAmount()) {
                        discount = coupon.getCouponMaxAmount();
                    }
                } else if ("A".equals(coupon.getCouponType())) {
                    discount = coupon.getCouponAmount();
                }
            }
        }

        return Math.max(basePrice - discount, 0);
    }
    
    public Order selectOrderById(String orderId) {
    	return orderDao.selectOrderById(orderId);
    }
    
    @Transactional
    public void refundSuccess(Refund refund) {
    	Map<String, Object> param = new HashMap<>();
    	param.put("refundId", refund.getRefundId());
    	param.put("orderId", refund.getOrderId());
    	orderDao.updateOrderRefund(param);
    	Order order = orderDao.selectOrderById(refund.getOrderId());
    	param.clear();
    	param.put("refundResult", "SUCCESS");
    	param.put("refundId", refund.getRefundId());
    	userCouponDao.updateUserCouponRefund(order.getOrderCouponId());
    	refundDao.updateRefundResult(param);
    	
    }
    
    @Transactional
    public void refundFail(Refund refund) {
    	Map<String, Object> param = new HashMap<>();
    	param.put("refundResult", "FAILED");
    	param.put("refundId", refund.getRefundId());
    	refundDao.updateRefundResult(param);
    }
    
    @Transactional
    public void orderSuccess(String orderId) {
    	Map<String, Object> param = new HashMap<>();
    	param.put("orderId", orderId);
    	param.put("orderStatus", "S");
    	orderDao.updateOrderStatus(param);
    }
    
    @Transactional
    public void orderFail(String orderId) {
    	Map<String, Object> param = new HashMap<>();
    	param.put("orderId", orderId);
    	param.put("orderStatus", "F");
    	orderDao.updateOrderStatus(param);
    }
    
    public void deleteExpiredOrders(String status) {
        Map<String, Object> param = new HashMap<>();
        param.put("status", status); // 결제 안 된 상태

        List<String> expiredOrderIds = orderDao.selectExpiredOrderIds(param);
        
        for (String orderId : expiredOrderIds) {
            // 하위 테이블 먼저 삭제 (외래키 cascade 없어도 안전하게 처리)
            orderDao.deleteOrderDetailsByOrderId(orderId);

            // 본 주문 삭제
            orderDao.deleteOrderById(orderId);
        }
    }
    
    // 유저 주문내역
    public List<Order> userOrderlist(String userId)
    {
    	List<Order> order = null;
    	
    	try
    	{
    		order = orderDao.userOrderlist(userId);
    	}
    	catch(Exception e)
    	{
    		logger.error("[OrderService] userOrderlist : ", e);
    	}
    	
    	return order;
    }
    
    //유저 주문상세
    public OrderDetail userOrderDetails(String orderId)
    {
    	OrderDetail orderdetail = null;
    	
    	try
    	{
    		orderdetail = orderDao.userOrderDetails(orderId);
    	}
    	catch(Exception e)
    	{
    		logger.error("[OrderService] userOrderDetails : ", e);
    	}
    	
    	return orderdetail;
    }
    
    // 체크인 날짜 하루 전인지
    public int userOrderCheckin(String orderId)
    {
    	int count = 0;
    	
    	try
    	{
    		count = orderDao.userOrderCheckin(orderId);
    	}
    	catch(Exception e)
    	{
    		logger.error("[OrderService] userOrderCheckin : ", e);
    	}
    	
    	return count;
    }
    
    // 체크아웃 날짜 한달 내인지
    public int userOrderCheckout(String orderId)
    {
    	int count = 0;
    	
    	try
    	{
    		count = orderDao.userOrderCheckout(orderId);
    	}
    	catch(Exception e)
    	{
    		logger.error("[OrderService] userOrderCheckout : ", e);
    	}
    	
    	return count;
    }
    
    public int calculateRefundAmount(Order order) {
        // 오늘 날짜
        LocalDate today = LocalDate.now();
        OrderDetail orderDetail = orderDao.userOrderDetails(order.getOrderId());
        // 체크인 날짜 (OrderDetail에서 가져오거나 order.getCheckInDate() 등으로)
        LocalDate checkInDate = orderDetail.getOrderDetailsCheckinDate().toInstant()
														                .atZone(ZoneId.systemDefault())
														                .toLocalDate(); // LocalDate여야 함

        // 남은 일수 계산
        long daysUntilCheckIn = ChronoUnit.DAYS.between(today, checkInDate);

        int totalAmount = order.getOrderTotalAmount();

        int refundAmount;
        if (daysUntilCheckIn >= 3) {
            refundAmount = totalAmount;
        } else if (daysUntilCheckIn == 2) {
            refundAmount = (int) (totalAmount * 0.8);
        } else if (daysUntilCheckIn == 1) {
            refundAmount = (int) (totalAmount * 0.5);
        } else {
            refundAmount = 0; // 체크인 당일이거나 지난 경우
        }

        return refundAmount;
    }
    
    // 판매자 페이지 정산 내역 띄워주는 거
    public List<OrderDetail> sellerSellList(String sellerId)
    {
    	List<OrderDetail> list = new ArrayList<>();
    	
    	try
    	{
    		list = orderDao.sellerSellList(sellerId);
    	}
    	catch(Exception e)
    	{
    		logger.error("[OrderService] sellerSellList : ", e);
    	}
    	
    	return list;
    }
    
    // 판매자 정산 총액 띄워주는 거
    public int sellerSum(String sellerId)
    {
    	int sum = 0;
    	
    	try
    	{
    		sum = orderDao.sellerSum(sellerId);
    	}
    	catch(Exception e)
    	{
    		logger.error("[OrderService] sellerSum : ", e);
    	}
    	
    	return sum;
    }
}
