package com.sist.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.sist.web.model.AccommodationRoom;
import com.sist.web.model.AccommodationRoomPrice;
import com.sist.web.service.AccommodationRoomPriceService;
import com.sist.web.service.AccommodationRoomService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

@Controller
public class AccommodationRoomPriceController {

    @Autowired
    private AccommodationRoomPriceService accommodationRoomPriceService;
    
    @Autowired
    private AccommodationRoomService accommodationRoomService;
    
//    @GetMapping("/syncRoomPrice")
//    public String RoomPrice() {
//        int totalCount = 0;
//        try {
//        	accommodationRoomPriceService.insertAllRoomPrice();
//        	return "객실 가격 동기화 완료";
//        } catch (Exception e) {   
//            return "객실 가격 동기화 중 에러 발생: " + e.getMessage();
//        }
//    }
    
    @GetMapping("/seller/roomPriceUpdateForm")
    public String roomPriceUpdateForm(@RequestParam("accommId") String accommId,
                                      Model model, HttpSession session) {
        String sellerId = (String) session.getAttribute("sellerId");
        if (sellerId == null) {
            return "redirect:/user/login";
        }

        // ① 숙소에 해당하는 객실 목록 조회
        List<AccommodationRoom> roomList = accommodationRoomService.searchByAccommid(accommId);

        // ② 모델에 담기
        model.addAttribute("roomList", roomList);
        model.addAttribute("accommId", accommId);

        // ③ JSP 반환 (예: /WEB-INF/views/seller/roomPriceUpdateForm.jsp)
        return "/seller/roomPriceUpdateForm";
    }
    
    @GetMapping("/roomPrice/list")
    @ResponseBody
    public List<Map<String, Object>> getRoomPriceList(@RequestParam("roomId") String roomId) {
        List<AccommodationRoomPrice> prices = accommodationRoomPriceService.getAccommodationRoomPrice(roomId);
        List<Map<String, Object>> result = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (AccommodationRoomPrice p : prices) {
            System.out.println("===== 객체 확인 =====");
            System.out.println("객실 ID: " + p.getAccommRoomId());
            System.out.println("Start: " + p.getAccommRoomPriceStart());
            System.out.println("End: " + p.getAccommRoomPriceEnd());
            System.out.println("Weekday: " + p.getAccommRoomPriceWeekday());
            System.out.println("Friday: " + p.getAccommRoomPriceFriday());
            System.out.println("Saturday: " + p.getAccommRoomPriceSaturday());
            System.out.println("Sunday: " + p.getAccommRoomPriceSunday());

            Map<String, Object> map = new HashMap<>();
            map.put("accommRoomPriceId", p.getAccommRoomPriceId());
            map.put("accommRoomPriceStart", 
                    p.getAccommRoomPriceStart() != null ? sdf.format(p.getAccommRoomPriceStart()) : "");
            map.put("accommRoomPriceEnd", 
                    p.getAccommRoomPriceEnd() != null ? sdf.format(p.getAccommRoomPriceEnd()) : "");
            map.put("accommRoomPriceWeekday", p.getAccommRoomPriceWeekday());
            map.put("accommRoomPriceFriday", p.getAccommRoomPriceFriday());
            map.put("accommRoomPriceSaturday", p.getAccommRoomPriceSaturday());
            map.put("accommRoomPriceSunday", p.getAccommRoomPriceSunday());

            result.add(map);
        }

        return result;
    }


    @PostMapping("/roomPrice/insert")
    @ResponseBody
    public Map<String, Object> insertRoomPrice(@RequestBody Map<String, Object> param) {
        Map<String, Object> res = new HashMap<>();

        try {
            String roomId = (String) param.get("roomId");
            String startDateStr = (String) param.get("startDate");
            String endDateStr = (String) param.get("endDate");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date startDate = sdf.parse(startDateStr);
            Date endDate = sdf.parse(endDateStr);

            // 중복 체크
            boolean isOverlap = accommodationRoomPriceService.isPriceDateOverlap(roomId, startDate, endDate);
            if (isOverlap) {
                res.put("status", "fail");
                res.put("message", "해당 기간은 이미 등록된 요금이 존재합니다.");
                return res;
            }

            // 객체 생성 및 값 세팅
            AccommodationRoomPrice price = new AccommodationRoomPrice();
            price.setAccommRoomId(roomId);
            price.setAccommRoomPriceStart(startDate);
            price.setAccommRoomPriceEnd(endDate);
            price.setAccommRoomPriceWeekday(Integer.parseInt(param.get("weekdayPrice").toString()));
            price.setAccommRoomPriceFriday(Integer.parseInt(param.get("fridayPrice").toString()));
            price.setAccommRoomPriceSaturday(Integer.parseInt(param.get("saturdayPrice").toString()));
            price.setAccommRoomPriceSunday(Integer.parseInt(param.get("sundayPrice").toString()));

            accommodationRoomPriceService.insertRoomPrice(price);

            res.put("status", "ok");
        } catch (Exception e) {
            res.put("status", "fail");
            res.put("message", "저장 중 오류 발생: " + e.getMessage());
        }

        return res;
    }
    
    @PostMapping("/roomPrice/delete")
    @ResponseBody
    public Map<String, Object> deleteRoomPrice(@RequestBody Map<String, Object> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            String priceId = (String) param.get("priceId");
            accommodationRoomPriceService.deleteRoomPrice(priceId);
            result.put("status", "ok");
        } catch (Exception e) {
            result.put("status", "fail");
            result.put("message", e.getMessage());
        }
        return result;
    }

}