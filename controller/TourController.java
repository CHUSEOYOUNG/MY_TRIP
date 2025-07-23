package com.sist.web.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.sist.web.model.Tour;
import com.sist.web.model.Accommodation;
import com.sist.web.model.Region;
import com.sist.web.model.Sigungu;
import com.sist.web.model.Tour;
import com.sist.web.service.RegionService;
import com.sist.web.service.SigunguService;
import com.sist.web.service.TourService;

@Controller("TourController")
public class TourController {

    @Autowired
    private TourService tourService;
    
    @Autowired
    private SigunguService sigunguService;
    
    @Autowired
    private RegionService regionService;
    @GetMapping("/syncTour")
    public String syncTour() {
        try {
            tourService.syncAllTours();  // 전체 시군구별 관광지 동기화 실행
            return "전체 관광지 동기화 완료.";
        } catch (Exception e) {
            e.printStackTrace();
            return "에러 발생: " + e.getMessage();
        }
    }

    @GetMapping("/listAll")
    @ResponseBody
    public List<Tour> getAllTours() {
        return tourService.getAllTours();
    }
    

    
    @GetMapping("/tour/list")
    public String listPage(@RequestParam(value = "regionId", required = false) String regionId,
                           Model model, HttpSession session) {

        List<Sigungu> sigunguList = sigunguService.getAllSigungus();
        List<Region>  regionList  = regionService.getAllRegions();

        model.addAttribute("sigunguList", sigunguList);
        model.addAttribute("regionList",  regionList);

        // 프론트에서 초기 regionId 를 사용하도록 전달
        if (regionId != null && !regionId.isEmpty()) {
            model.addAttribute("selectedRegionId", regionId);
        }
        return "/tour/list";
    }
    
    @PostMapping("/tour/filterList")
    public String filterList(@RequestBody Map<String, Object> requestBody,
                             HttpSession session,
                             Model model) {
        String userId = (String) session.getAttribute("userId");

        // 1. 페이지 파라미터 안전하게 받기
        Number rawPage = (Number) requestBody.getOrDefault("page", 1);
        int page = rawPage.intValue();

        // 2. 시군구 리스트 파싱
        List<Map<String, Object>> rawList = (List<Map<String, Object>>) requestBody.get("sigunguList");
        List<Sigungu> sigunguList = rawList.stream().map(map -> {
            Sigungu s = new Sigungu();
            s.setRegionId((String) map.get("regionId"));
            s.setSigunguId((String) map.get("sigunguId"));
            return s;
        }).collect(Collectors.toList());

        // 3. 페이징 기본 설정
        int pageSize = 20;
        int pageBlockSize = 10;

        // 4. 전체 개수 및 페이지 계산
        int totalCount = tourService.getTourCount(sigunguList);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        // 5. 블럭 페이징 계산
        int startPage = ((page - 1) / pageBlockSize) * pageBlockSize + 1;
        int endPage = Math.min(startPage + pageBlockSize - 1, totalPages);
        boolean hasPrev = startPage > 1;
        boolean hasNext = endPage < totalPages;

        // 6. 데이터 조회
        List<Tour> results = tourService.findBySigunguList(sigunguList, userId, page);

        // 7. 모델에 추가
        model.addAttribute("results", results);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("hasNext", hasNext);

        return "/tour/cardList";
    }

}
