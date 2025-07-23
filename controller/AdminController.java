package com.sist.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;

import com.sist.web.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.sist.common.util.StringUtil;
import com.sist.web.model.Admin;
import com.sist.web.model.Coupon;
import com.sist.web.model.Event;
import com.sist.web.model.Notice;
import com.sist.web.model.Seller;
import com.sist.web.service.AdminService;
import com.sist.web.service.EventService;
import com.sist.web.service.SellerService;

@Controller("adminController")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private SellerService sellerService;

    // /admin 했을 때 어드민 페이지 로그인으로 가게
    @RequestMapping("/admin")
    public String admin() {
        return "/admin/adminLogin";
    }
    
    @RequestMapping("/admin/adminLogin")
    public String adminLoginPage() {
        return "/admin/adminLogin";
    }

    @RequestMapping("/admin/loginProc")
    @ResponseBody
    public Map<String, Object> adminLoginProc(HttpServletRequest request) {
        String adminId = request.getParameter("adminId");
        String adminPassword = request.getParameter("adminPassword");

        Map<String, Object> result = new HashMap<>();

        if (StringUtil.isEmpty(adminId) || StringUtil.isEmpty(adminPassword)) {
            result.put("code", 400);
            return result;
        }

        Admin admin = adminService.getAdminById(adminId);

        if (admin == null) {
            result.put("code", 404);
        } else if (!admin.getAdminPassword().equals(adminPassword)) {
            result.put("code", -1);
        } else {
            request.getSession().setAttribute("adminLogin", admin);
            result.put("code", 0);
        }

        return result;
    }

    @RequestMapping("/admin/dashboard")
    public String adminDashboardPage() {
        return "/admin/dashboard";
    }

    @RequestMapping("/admin/logout")
    public String adminLogout(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/admin/adminLogin";
    }

    /**
     * ✅ 판매자 목록 페이지
     */
    @GetMapping("/admin/sellerList")
    public String sellerList(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             Model model) {

        int pageSize = 10;
        int startRow = (page - 1) * pageSize + 1;
        int endRow = page * pageSize;

        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("start", startRow);
        param.put("end", endRow);

        List<Seller> sellerList = adminService.getSellerList(param);
        int totalCount = adminService.getSellerCount(param);
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);

        model.addAttribute("sellerList", sellerList);
        model.addAttribute("curPage", page);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("totalCount", totalCount);

        return "/admin/sellerList";
    }

    /**
     * ✅ 판매자 승인/취소 처리 (status: 'Y' 또는 'N')
     */
    @PostMapping("/admin/toggleSellerStatus")
    @ResponseBody
    public Map<String, Object> toggleSellerStatus(HttpServletRequest request) {
        String sellerId = request.getParameter("sellerId");
        String status = request.getParameter("status"); // Y or N

        Map<String, Object> result = new HashMap<>();
        if (StringUtil.isEmpty(sellerId) || StringUtil.isEmpty(status)) {
            result.put("code", 400);
            return result;
        }

        Map<String, Object> param = new HashMap<>();
        param.put("sellerId", sellerId);
        param.put("status", status);

        int updateCount = adminService.updateSellerStatus(param);
        result.put("code", updateCount > 0 ? 0 : -1);
        return result;
    }
    // ✅ 회원 목록 페이지
    @GetMapping("/admin/userList")
    public String userList(@RequestParam(value = "keyword", required = false) String keyword,
                           @RequestParam(value = "page", defaultValue = "1") int curPage,
                           Model model) {

        int pageSize = 10;
        int startRow = (curPage - 1) * pageSize;
        int endRow = curPage * pageSize;
        // 검색 및 페이징 파라미터 Map
        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("startRow", startRow);
        param.put("endRow", endRow);

        // 검색된 사용자 목록 및 총 카운트
        List<User> userList = adminService.getUsersWithPaging(param);
        int totalCount = adminService.getUserCount(param);
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);

        // JSP에 데이터 전달
        model.addAttribute("userList", userList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("totalCount", totalCount);

        return "/admin/userList";
    }

    // ✅ 회원 탈퇴/복구 처리
    @PostMapping("/admin/toggleUserStatus")
    @ResponseBody
    public Map<String, Object> toggleUserStatus(@RequestParam String userId, @RequestParam String status) {
        boolean success = adminService.updateUserStatus(userId, status);

        Map<String, Object> result = new HashMap<>();
        result.put("code", success ? 0 : -1);
        return result;
    }
    
    
 // ✅ 숙소 목록 조회 (전체 or 요청 대기만 조회)
    @GetMapping("/admin/accommList")
    public String accommList(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "status", required = false) String status,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             Model model) {
    	 System.out.println("▶ 숙소 리스트 요청: keyword=" + keyword + ", status=" + status + ", page=" + page);
    	 if ("ALL".equals(status)) {
    		    status = null;
    		}

        int pageSize = 10;
        int startRow = (page - 1) * pageSize + 1;
        int endRow = page * pageSize;
        
        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("status", status); // 'N' 또는 null
        param.put("start", startRow);
        param.put("end", endRow);

        List<Map<String, Object>> accommList = adminService.getAccommList(param);
        int totalCount = adminService.getAccommCount(param);
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);
        System.out.println("▶ DAO로 전달될 param: " + param);
        
        model.addAttribute("accommList", accommList);
        model.addAttribute("curPage", page);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        model.addAttribute("totalCount", totalCount);

        
        
        int blockSize = 10;
        int blockStart = ((page - 1) / blockSize) * blockSize + 1;
        int blockEnd = Math.min(blockStart + blockSize - 1, totalPage);

        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        
        return "/admin/accommList";
    }
    
 // ✅ 숙소 승인 처리 (ACCOMM_STATUS = 'Y'로 변경)
    @PostMapping("/admin/approveAccomm")
    @ResponseBody
    public Map<String, Object> approveAccomm(@RequestParam String accommId) {
        Map<String, Object> result = new HashMap<>();

        boolean success = adminService.approveAccomm(accommId); // → ACCOMM_STATUS = 'Y' 업데이트
        result.put("code", success ? 0 : -1);

        return result;
    }
    
 // ✅ 리뷰 목록
    @GetMapping("/admin/reviewList")
    public String reviewList(
            @RequestParam(value="keyword", required=false) String keyword,
            @RequestParam(value="order",   defaultValue="recent") String order,
            @RequestParam(value="status",  required=false) String status, // ✅ 추가
            @RequestParam(value="page",    defaultValue="1") int page,
            Model model) {

        if (status == null || status.isEmpty()) {
            status = "Y"; // 기본값 공개
        }

        int pageSize = 10;
        int startRow = (page-1)*pageSize + 1;
        int endRow   =  page   *pageSize;

        Map<String,Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("order",   order);
        param.put("start",   startRow);
        param.put("end",     endRow);
        param.put("status",  status); // ✅ 동적 처리

        List<Map<String,Object>> reviewList = adminService.getReviewList(param);
        int totalCount = adminService.getReviewCount(param);
        int totalPage  = (int)Math.ceil((double)totalCount / pageSize);

        int blockSize   = 10;
        int blockStart  = ((page-1)/blockSize)*blockSize + 1;
        int blockEnd    = Math.min(blockStart + blockSize - 1, totalPage);

        model.addAttribute("reviewList", reviewList);
        model.addAttribute("keyword",    keyword);
        model.addAttribute("order",      order);
        model.addAttribute("status",     status); // ✅ 상태도 전달
        model.addAttribute("curPage",    page);
        model.addAttribute("totalPage",  totalPage);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd",   blockEnd);

        return "/admin/reviewList";
    }


    
    @PostMapping("/admin/updateReviewStatus")
    @ResponseBody
    public Map<String, Object> updateReviewStatus(@RequestBody Map<String, Object> param) {
        // planId → float형처럼 들어온 걸 int로 변환
        String planIdStr = String.valueOf(param.get("planId")); // 예: "90.0"
        int planId = (int) Double.parseDouble(planIdStr);       // 90.0 → 90

        String status = (String) param.get("status");

        int result = adminService.updateReviewStatus(planId, status);

        Map<String, Object> response = new HashMap<>();
        if (result > 0) {
            response.put("code", 0);
            response.put("msg", "성공");
        } else {
            response.put("code", 1);
            response.put("msg", "실패");
        }
        return response;
    }
    
    @GetMapping("/admin/noticeList")
    public String noticeList(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "page", defaultValue = "1") int page,
                             Model model) {
        int pageSize = 10;
        int startRow = (page - 1) * pageSize;
        int endRow = page * pageSize;

        Map<String, Object> param = new HashMap<>();
        param.put("keyword", keyword);
        param.put("startRow", startRow);
        param.put("pageSize", pageSize);

        List<Notice> noticeList = adminService.searchNoticeList(param);
        int totalCount = adminService.getSearchNoticeCount(param);
        int totalPage = (int) Math.ceil((double) totalCount / pageSize);

        int blockSize = 10;
        int blockStart = ((page - 1) / blockSize) * blockSize + 1;
        int blockEnd = Math.min(blockStart + blockSize - 1, totalPage);

        model.addAttribute("noticeList", noticeList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("curPage", page);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        model.addAttribute("totalCount", totalCount);

        return "/admin/noticeList";
    }

    @GetMapping("/admin/noticeWriteForm")
    public String noticeWriteForm() {
        return "/admin/noticeWriteForm";  // 위의 JSP 위치
    }

    @PostMapping("/admin/noticeWriteProc")
    public String noticeWriteProc(@RequestParam Map<String, Object> param) {
        adminService.insertNotice(param);  // 작성 로직
        return "redirect:/admin/noticeList";
    }
    
    @PostMapping("/admin/noticeUpdate")
    @ResponseBody
    public Map<String, Object> updateNotice(@RequestBody Notice notice) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.updateNotice(notice);
            result.put("code", 0);
        } catch (Exception e) {
            result.put("code", -1);
            result.put("msg", "수정 중 오류 발생");
        }
        return result;
    }
    
    @GetMapping("/admin/noticeUpdateForm")
    public String showUpdateForm(@RequestParam("noticeId") String noticeId, Model model) {
        Notice notice = adminService.getNoticeById(noticeId);
        model.addAttribute("notice", notice);
        return "/admin/noticeUpdateForm";
    }
    
    @PostMapping("/admin/noticeDelete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotice(@RequestParam("noticeId") String noticeId) {
        Map<String, Object> result = new HashMap<>();
        try {
            int deleted = adminService.deleteNotice(noticeId);  // 삭제 처리
            if (deleted > 0) {
                result.put("code", 0);
                result.put("msg", "삭제 성공");
            } else {
                result.put("code", -1);
                result.put("msg", "삭제 대상 없음");
            }
        } catch (Exception e) {
            result.put("code", -500);
            result.put("msg", "서버 오류");
        }
        return ResponseEntity.ok(result);
    }
    


    // ✅ 이벤트 리스트 페이지 이동
    @GetMapping("/admin/eventList")
    public String eventList(ModelMap model,
                            @RequestParam(value = "page", defaultValue = "1") int curPage,
                            @RequestParam(value = "keyword", required = false) String keyword) {
        final int pageSize = 10;
        final int blockSize = 5;

        int startRow = (curPage - 1) * pageSize;

        Map<String, Object> param = new HashMap<>();
        param.put("startRow", startRow);
        param.put("pageSize", pageSize);
        param.put("keyword", keyword);

        List<Event> list = adminService.searchEventList(param);
        int totalCount = adminService.getSearchEventCount(keyword);
        int totalPage = (int) Math.ceil(totalCount / (double) pageSize);

        int blockStart = ((curPage - 1) / blockSize) * blockSize + 1;
        int blockEnd = blockStart + blockSize - 1;
        if (blockEnd > totalPage) blockEnd = totalPage;

        model.addAttribute("eventList", list);
        model.addAttribute("curPage", curPage);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("blockStart", blockStart);
        model.addAttribute("blockEnd", blockEnd);
        model.addAttribute("keyword", keyword);

        return "/admin/eventList";
    }

    // ✅ 작성 폼 Ajax 로딩
    @GetMapping("/admin/eventWriteForm")
    public String eventWriteForm(ModelMap model) {
        List<Coupon> couponList = adminService.getAllCoupons();
        model.addAttribute("couponList", couponList);
        return "/admin/eventWriteForm";
    }


    // ✅ 수정 폼 Ajax 로딩
    @GetMapping("/admin/eventUpdateForm")
    public String eventUpdateForm(@RequestParam("eventId") String eventId, ModelMap model) {
        Event event = adminService.getEventById(eventId);
        List<Coupon> couponList = adminService.getAllCoupons();

        model.addAttribute("event", event);
        model.addAttribute("couponList", couponList);
        return "/admin/eventUpdateForm";
    }

    // ✅ 이벤트 수정 처리
    @PostMapping("/admin/eventUpdate")
    public String eventUpdate(Event event) {
        adminService.updateEvent(event);
        return "redirect:/admin/eventList";
    }

    @PostMapping("/admin/eventDelete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eventDelete(@RequestParam("eventId") String eventId) {
        Map<String, Object> result = new HashMap<>();
        try {
            adminService.deleteEvent(eventId);
            result.put("code", 0);
            result.put("msg", "삭제 완료");
        } catch (Exception e) {
            result.put("code", -500);
            result.put("msg", "서버 오류 발생");
        }
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/admin/eventInsert")
    public String insertEvent(MultipartHttpServletRequest request) {
        String eventTitle = request.getParameter("eventTitle");
        String couponId = request.getParameter("couponId");
        String endDate = request.getParameter("eventEnddate");

        //  시퀀스로 ID 생성 
        String eventId = adminService.getNextEventId(); // → "EVT" + 시퀀스 값

        // ✅ 이미지 저장 경로
        String thumbDir = "C:\\project\\webapps\\finalproject\\src\\main\\webapp\\WEB-INF\\views\\resources\\eventimage\\";
        
        String detailDir = "C:\\project\\webapps\\finalproject\\src\\main\\webapp\\WEB-INF\\views\\resources\\eventdetailimage\\";

        try {
            // 썸네일 저장
            MultipartFile thumbFile = request.getFile("eventThumbnail");
            if (thumbFile != null && !thumbFile.isEmpty()) {
                thumbFile.transferTo(new File(thumbDir + File.separator + eventId + ".png"));
            }

            // 본문 이미지 저장
            MultipartFile detailFile = request.getFile("eventDetailImage");
            if (detailFile != null && !detailFile.isEmpty()) {
                detailFile.transferTo(new File(detailDir + File.separator + eventId + ".png"));
            }

            // DB 저장
            Event event = new Event();
            event.setEventId(eventId);
            event.setEventTitle(eventTitle);
            event.setCouponId(couponId);
            event.setEventEnddate(endDate);
            event.setEventThumbnailUrl("/resources/eventimage/" + eventId + ".png");
            event.setEventImageUrl("/resources/eventdetailimage/" + eventId + ".png");

            adminService.insertEvent(event);

        } catch (Exception e) {
            e.printStackTrace();
            return "/error/500";
        }

        return "redirect:/admin/eventList";
    }
    
}
