package com.sist.web.controller;

import com.sist.web.model.Sigungu;
import com.sist.web.service.SigunguService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SigunguController {

    @Autowired
    private SigunguService sigunguService;

    @GetMapping("/syncSigungu")
    public String syncSigungu() {
        try {
            for (int i = 1; i <= 39; i++) {
                List<Sigungu> sigunguList = sigunguService.fetchSigunguByRegion(i + "");
                sigunguService.saveSigunguList(sigunguList);
                System.out.println(i + "번 지역 시군구 수집 완료");
            }
            return "시군구 데이터 동기화 완료";
        } catch (Exception e) {
            e.printStackTrace();
            return "시군구 동기화 중 오류 발생: " + e.getMessage();
        }
    }
}
