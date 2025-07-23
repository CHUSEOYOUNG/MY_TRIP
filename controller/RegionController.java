package com.sist.web.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sist.web.model.Region;
import com.sist.web.service.RegionService;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping("/syncRegion/page")
    public String syncRegion() {
        try {
            // API 호출 (예: 페이지 1, 10개)
            List<Region> list = regionService.fetchRegions(1, 10);
            
            // DB 저장
            regionService.saveRegionList(list);
            
            return "지역 동기화 완료: " + list.size() + "건 저장됨.";
        } catch (Exception e) {
            e.printStackTrace();
            return "에러 발생: " + e.getMessage();
        }
    }
    
    @GetMapping("/syncRegion/all")
    public String syncAllRegion() {
        try {
            regionService.syncAllRegions();
            return "전체 지역 동기화 완료!";
        } catch (Exception e) {
            e.printStackTrace();
            return "에러 발생: " + e.getMessage();
        }
    }

    
    @GetMapping("/syncRegion/subRegions")
    public String syncsubRegions() {
        try {
        	for(int i=1; i<=39; i++) {
        		regionService.fetchSubRegions(i);
        	}
            
            return "전체 지역 동기화 완료!";
        } catch (Exception e) {
            e.printStackTrace();
            return "에러 발생: " + e.getMessage();
        }
    }
}