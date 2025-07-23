package com.sist.web.controller;

import com.sist.common.util.StringUtil;
import com.sist.web.model.*;
import com.sist.web.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/order/kakao")
public class KakaoPayController {

    private static final Logger logger = LoggerFactory.getLogger(KakaoPayController.class);

    @Autowired private KakaoPayService kakaoPayService;
    @Autowired private AccommodationRoomService accommodationRoomService;
    @Autowired private AccommodationRoomPriceService accommodationRoomPriceService;
    @Autowired private UserCouponService couponService;
    @Autowired private OrderService orderService;
    @Autowired private RefundService refundService;

    @Value("#{env['kakaopay.tid.session.name']}")
    private String KAKAOPAY_TID_SESSION_NAME;

    @Value("#{env['kakaopay.orderid.session.name']}")
    private String KAKAOPAY_ORDERID_SESSION_NAME;

    @PostMapping("/start")
    @ResponseBody
    public Map<String, Object> kakaoPayStart(HttpServletRequest request, @RequestBody Map<String, String> param) {
        Map<String, Object> res = new HashMap<>();
        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                res.put("status", -9);
                res.put("message", "세션이 유효하지 않습니다.");
                return res;
            }

            String userId = (String) session.getAttribute("userId");
            String roomId = param.get("roomId");
            String checkIn = param.get("checkIn");
            String checkOut = param.get("checkOut");
            String userCouponId = param.get("userCouponId");

            AccommodationRoom room = accommodationRoomService.searchByAccommRoomId(roomId);
            if (room == null) {
                res.put("status", -1);
                res.put("message", "객실 정보를 찾을 수 없습니다.");
                return res;
            }

            int basePrice = (int) accommodationRoomPriceService
                    .calculateTotalPrice(roomId, checkIn, checkOut)
                    .getTotalPrice();
            int discount = 0;

            if (userCouponId != null && !userCouponId.isEmpty()) {
                UserCoupon coupon = couponService.getUserCouponById(userCouponId);
                if (coupon != null && userId.trim().equals(coupon.getUserId().trim())) {
                    String type = coupon.getCouponType();
                    if ("P".equalsIgnoreCase(type) || "PERCENT".equalsIgnoreCase(type)) {
                        discount = (int) (basePrice * (coupon.getCouponAmount() / 100.0));
                        if (coupon.getCouponMaxAmount() > 0 && discount > coupon.getCouponMaxAmount()) {
                            discount = coupon.getCouponMaxAmount();
                        }
                    } else if ("A".equalsIgnoreCase(type) || "AMOUNT".equalsIgnoreCase(type)) {
                        discount = coupon.getCouponAmount();
                    }
                } else {
                    logger.warn("쿠폰 조회 실패 또는 사용자 불일치: couponId={}, userId={}", userCouponId, userId);
                }
            }

            int finalPrice = Math.max(basePrice - discount, 0);
            String orderId = UUID.randomUUID().toString();

            KakaoPayReadyRequest req = new KakaoPayReadyRequest();
            req.setPartner_order_id(orderId);
            req.setPartner_user_id(userId);
            req.setItem_name(room.getRoomName());
            req.setItem_code(roomId);
            req.setQuantity(1);
            req.setTotal_amount(finalPrice);
            req.setTax_free_amount(0);
            
            KakaoPayReadyResponse kakaoRes = kakaoPayService.ready(req);
            if (kakaoRes == null) {
                res.put("status", -1);
                res.put("message", "카카오페이 결제 준비 실패");
                return res;
            }
            // 주문 및 상세 저장
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setOrderDate(Date.valueOf(LocalDate.now()));
            order.setOrderTotalAmount(finalPrice);
            order.setOrderStatus("W");
            order.setOrderCouponId(userCouponId);
            order.setOrderTid(kakaoRes.getTid());
            
            OrderDetail detail = new OrderDetail();
            detail.setOrderDetailsId(UUID.randomUUID().toString());
            detail.setOrderId(orderId);
            detail.setAccommRoomId(roomId);
            detail.setOrderDetailsCheckinDate(Date.valueOf(checkIn));
            detail.setOrderDetailsCheckoutDate(Date.valueOf(checkOut));
            detail.setOrderDetailsRoomEachPrice(finalPrice);
            detail.setOrderDetailsPeopleCount(2); // 인원 수 추후 반영
            detail.setOrderDetailsPaymentMethod("KAKAOPAY");
            detail.setOrderDetailsCount(1);

            boolean orderCheck = orderService.insertOrder(order, detail, checkIn, checkOut);
            if(!orderCheck) {
            	res.put("status", -3);
                res.put("message", "예약할 수 없는 객실입니다.");
                return res;
            }

            // 세션 저장
            session.setAttribute("order", order);
            session.setAttribute("orderDetail", detail);
            session.setAttribute(KAKAOPAY_TID_SESSION_NAME, kakaoRes.getTid());
            session.setAttribute(KAKAOPAY_ORDERID_SESSION_NAME, orderId);

            res.put("status", 0);
            res.put("nextRedirectPcUrl", kakaoRes.getNext_redirect_pc_url());
            return res;
        } catch (Exception e) {
            logger.error("[KakaoPayController] kakaoPayStart error", e);
            res.put("status", -99);
            res.put("message", "결제 요청 처리 중 오류가 발생했습니다.");
            return res;
        }
    }

    @GetMapping("/success")
    public String kakaoPaySuccess(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            model.addAttribute("code", -99);
            model.addAttribute("msg", "세션이 만료되었습니다.");
            return "/order/fail";
        }

        String userId = (String) session.getAttribute("userId");
        String pg_token = request.getParameter("pg_token");
        String tid = (String) session.getAttribute(KAKAOPAY_TID_SESSION_NAME);
        String orderId = (String) session.getAttribute(KAKAOPAY_ORDERID_SESSION_NAME);

        
        
        if (StringUtil.isEmpty(pg_token) || StringUtil.isEmpty(tid)) {
            model.addAttribute("code", -1);
            model.addAttribute("msg", "결제 승인 정보 누락");
            return "/order/fail";
        }

        KakaoPayApproveRequest approveReq = new KakaoPayApproveRequest();
        approveReq.setTid(tid);
        approveReq.setPartner_order_id(orderId);
        approveReq.setPartner_user_id(userId);
        approveReq.setPg_token(pg_token);

        KakaoPayApproveResponse approveRes = kakaoPayService.approve(approveReq);
        if (approveRes == null || approveRes.getError_code() != 0) {
            model.addAttribute("code", -2);
            model.addAttribute("msg", "카카오페이 승인 실패");
            return "/order/fail";
        }


        Order order = (Order) session.getAttribute("order");
        OrderDetail detail = (OrderDetail) session.getAttribute("orderDetail");
        // 세션 정리
        session.removeAttribute(KAKAOPAY_TID_SESSION_NAME);
        session.removeAttribute(KAKAOPAY_ORDERID_SESSION_NAME);

        model.addAttribute("order", order);
        model.addAttribute("detail", detail);

        model.addAttribute("code", 0);
        model.addAttribute("msg", "결제가 완료되었습니다.");
        model.addAttribute("orderId", orderId);
        
        orderService.orderSuccess(orderId);
        
        return "/order/kakaoSuccessPopup";
    }

    @GetMapping("/fail")
    public String kakaoPayFail(HttpServletRequest request, Model model) {
    	HttpSession session = request.getSession(false);
        model.addAttribute("code", -1);
        model.addAttribute("msg", "결제에 실패했습니다.");
        String orderId = (String) session.getAttribute(KAKAOPAY_ORDERID_SESSION_NAME);
        orderService.orderFail(orderId);
        return "/order/fail";
    }

    @GetMapping("/cancel")
    public String kakaoPayCancel(HttpServletRequest request, Model model) {
    	HttpSession session = request.getSession(false);
        model.addAttribute("code", -2);
        model.addAttribute("msg", "결제가 취소되었습니다.");
        String orderId = (String) session.getAttribute(KAKAOPAY_ORDERID_SESSION_NAME);
        orderService.orderFail(orderId);
        return "/order/fail";
    }
    
    @GetMapping("/complete")
    public String paymentCompletePage(HttpSession session, Model model) {
        Order order = (Order) session.getAttribute("order");
        OrderDetail detail = (OrderDetail) session.getAttribute("orderDetail");

        if (order == null || detail == null) {
            return "redirect:/"; // 또는 오류 처리
        }

        Coupon coupon = null;
        int discountAmount = 0;
        int originAmount = order.getOrderTotalAmount();

        String couponId = order.getOrderCouponId();
        if (couponId != null && !couponId.isEmpty()) {
            coupon = couponService.selectCoupon(couponId);
            if (coupon != null) {
                discountAmount = couponService.calculateDiscount(order.getOrderTotalAmount(), coupon);
                originAmount += discountAmount;
            }
        }

        model.addAttribute("coupon", coupon);
        model.addAttribute("originAmount", originAmount);
        model.addAttribute("discountAmount", discountAmount);
        model.addAttribute("order", order);
        model.addAttribute("detail", detail);

        // 사용 후 세션 제거
        session.removeAttribute("order");
        session.removeAttribute("detail");

        return "/order/complete";
    }

    
    
    @PostMapping("/cancel/refund")
    @ResponseBody
    public Map<String, Object> kakaoPayRefund(@RequestParam("orderId") String orderId, HttpSession session) {
        Map<String, Object> res = new HashMap<>();

        try {
            String userId = (String) session.getAttribute("userId");

            if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(orderId)) {
                res.put("status", -9);
                res.put("message", "세션 또는 파라미터가 유효하지 않습니다.");
                return res;
            }


            Order order = orderService.selectOrderById(orderId);
            if (order == null || !userId.equals(order.getUserId())) {
                res.put("status", -1);
                res.put("message", "해당 주문이 존재하지 않거나 접근 권한이 없습니다.");
                return res;
            }
            
            if(!order.getUserId().equals(userId)) {
            	res.put("status", -2);
                res.put("message", "주문자의 ID까 아닙니다.");
            }

            String tid = order.getOrderTid();
            Refund refund = new Refund();
            refund.setOrderId(orderId);
            refund.setUserId(userId);
            refundService.inserRefund(refund);
            // refund의 현재 상태 : requested인 상태로 생성, 환불 금액은 전체로 설정, ID는 시퀀스로 추가
            // 금액에 대한 로직 추가
            int refundAmount = orderService.calculateRefundAmount(order);
            boolean success = kakaoPayService.cancel(tid, refundAmount);

            if (success) {
            	orderService.refundSuccess(refund);

                res.put("status", 0);
                res.put("message", "환불이 정상 처리되었습니다.");
            } else {
            	orderService.refundFail(refund);
                res.put("status", -2);
                res.put("message", "카카오페이 환불 처리에 실패했습니다.");
            }

        } catch (Exception e) {
        	
            logger.error("[KakaoPayController] kakaoPayRefund error", e);
            res.put("status", -99);
            res.put("message", "환불 처리 중 오류 발생");
        }

        return res;
    }

    
    
    
 
}
