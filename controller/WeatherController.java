package com.sist.web.controller;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.ui.Model;

import com.sist.web.service.WeatherService;
import com.sist.web.model.WeatherInfo;


@Controller("WeatherController")
public class WeatherController
{
	private static Logger logger = LoggerFactory.getLogger(WeatherController.class);

    @Autowired
    private WeatherService weatherService;
	
	@RequestMapping(value = "/weather", method=RequestMethod.GET)
	public String weather(Model model, HttpServletRequest request, HttpServletResponse response)
	{
		logger.debug("[날씨] 진입 성공");

        List<WeatherInfo> weatherList = new ArrayList<>();

        // 주요 도시들 좌표 + 이름
        String[] cityNames = {
            "서울", "인천", "대전", "대구", "광주", "부산", "울산", "세종", 
            "경기도", "강원특별자치도", "충청북도", "충청남도", "경상북도", 
            "경상남도", "전북특별자치도", "전라남도", "제주도"
        };
        double[][] coords = {
            {37.5667, 126.9784},    // 서울
            {37.4563, 126.7052},    // 인천
            {36.3504, 127.3845},    // 대전
            {35.8714, 128.6014},    // 대구
            {35.1595, 126.8526},    // 광주
            {35.1796, 129.0756},    // 부산
            {35.5384, 129.3114},    // 울산
            {36.5041, 127.2494},    // 세종
            {38.3006, 126.2620},    // 경기도
            {37.8228, 128.1555},    // 강원
            {36.85, 127.85},        // 충북
            {36.55, 127},           // 충남
            {36.664, 128.434},      // 경북
            {35.041, 128.049},      // 경남
            {35.833, 127.141},      // 전북
            {34.764, 127.663},      // 전남
            {33.500, 126.503}       // 제주
        };

        // 반복적으로 List 에 담는다
        for (int i = 0; i < cityNames.length; i++) {
            WeatherInfo info = weatherService.getWeather(coords[i][0], coords[i][1]);
            info.setCity(cityNames[i]);
            weatherList.add(info);
        }

        model.addAttribute("weatherList", weatherList);

        return "/weather";
	}

}
