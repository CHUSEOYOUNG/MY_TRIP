package com.sist.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sist.web.model.Notice;
import com.sist.web.service.NoticeService;


@Controller("noticeController")
public class NoticeController {

	@Autowired
    private NoticeService noticeService;
	
	private static final Logger logger = LoggerFactory.getLogger(EventController.class);
	
	
	 @RequestMapping("/notice/noticeList")
	    public String noticeList(@RequestParam(value="page", defaultValue="1") int page, Model model) {
	        int pageSize = 10;
	        int startRow = (page - 1) * pageSize;

	        List<Notice> noticeList = noticeService.getNoticeList(startRow, pageSize);
	        int totalCount = noticeService.getNoticeCount();
	        
	        model.addAttribute("noticeList", noticeList);
	        model.addAttribute("totalCount", totalCount);
	        model.addAttribute("currentPage", page);
	        model.addAttribute("pageSize", pageSize);
	        // 🔍 로그로 디버깅
	        System.out.println("총 개수: " + totalCount);
	        System.out.println("현재 페이지: " + page);
	        System.out.println("페이지 크기: " + pageSize);
	        for (int i = 0; i < noticeList.size(); i++) {
	            Notice e = noticeList.get(i);
	            System.out.println("[" + i + "] ID: " + e.getNoticeId() + ", 제목: " + e.getNoticeTitle() + ", 날짜: " + e.getNoticeRegdate());
	        }
	        
	        int totalPage = (int) Math.ceil((double) totalCount / pageSize);
	        model.addAttribute("totalPage", totalPage);
	        
	        return "/notice/noticeList"; // /WEB-INF/views/notice/noticeList.jsp
	    }
	 
	 @RequestMapping("/notice/ajaxSearch")
	 @ResponseBody
	 public Map<String, Object> ajaxSearchNotice(
	         @RequestParam(name = "searchKeyword", required = false) String keyword,
	         @RequestParam(name = "page", defaultValue = "1") int curPage) {

	     Map<String, Object> result = new HashMap<>();
	     int pageSize = 10;
	     int startRow = (curPage - 1) * pageSize;

	     // 🔧 파라미터 설정
	     Map<String, Object> param = new HashMap<>();
	     param.put("keyword", keyword);
	     param.put("startRow", startRow);
	     param.put("pageSize", pageSize);

	     // 🔍 데이터 조회
	     int totalCount = noticeService.getSearchNoticeCount(param);
	     int totalPage = (int) Math.ceil((double) totalCount / pageSize);
	     int startNum = totalCount - startRow;
	     List<Notice> noticeList = noticeService.searchNoticeList(param);

	     // 🔧 HTML 테이블 동적 생성
	     StringBuilder tableHtml = new StringBuilder();
	     for (int i = 0; i < noticeList.size(); i++) {
	    	    Notice notice = noticeList.get(i);
	    	    int num = startRow + i + 1;

	    	    tableHtml.append("<tr>");
	    	    tableHtml.append("<td>").append(num).append("</td>");
	    	    tableHtml.append("<td class='title-col'>")
	    	             .append("<a href='/notice/view?noticeId=").append(notice.getNoticeId()).append("'>")
	    	             .append(notice.getNoticeTitle()).append("</a></td>");
	    	    tableHtml.append("<td>").append(notice.getNoticeCount()).append("</td>");

	    	    // ✅ 안전한 날짜 파싱
	    	    String regDateStr = "";
	    	    if (notice != null && notice.getNoticeRegdate() != null && notice.getNoticeRegdate().length() >= 10) {
	    	        regDateStr = notice.getNoticeRegdate().substring(0, 10);
	    	    }

	    	    tableHtml.append("<td>").append(regDateStr).append("</td>");
	    	    tableHtml.append("</tr>");
	    	}

	     // 🔧 페이지네이션 HTML 생성
	     StringBuilder paginationHtml = new StringBuilder();

	     if (curPage > 1) {
	         paginationHtml.append("<a href='?page=").append(curPage - 1).append("' class='prev'>« 이전</a>");
	     }

	     for (int i = 1; i <= totalPage; i++) {
	         paginationHtml.append("<a href='?page=").append(i).append("'")
	                       .append(i == curPage ? " class='active'" : "")
	                       .append(">").append(i).append("</a>");
	     }

	     if (curPage < totalPage) {
	         paginationHtml.append("<a href='?page=").append(curPage + 1).append("' class='next'>다음 »</a>");
	     }

	     result.put("tableHtml", tableHtml.toString());
	     result.put("paginationHtml", paginationHtml.toString());

	     return result;
	 }
	 
	 
	 @RequestMapping("/notice/noticeDetail")
	 public String noticeDetail(@RequestParam("noticeId") String noticeId, ModelMap model) {
	     Notice notice = noticeService.selectNoticeById(noticeId);
	     model.addAttribute("notice", notice);
	     return "/notice/noticeDetail";
	 }
	
}
