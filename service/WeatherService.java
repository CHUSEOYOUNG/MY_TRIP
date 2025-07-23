package com.sist.web.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sist.web.util.GeoUtil;
import com.sist.web.model.WeatherInfo;

@Service
public class WeatherService {

    private final String SERVICE_KEY = "MHbE44hd7kHCOFVk5VucNc1XiPWTzxPAraI2RBlclEk8DFoZtBZWPX1gTMSVJ5j1U6ggq3bD6eViUAyxJfBpdQ==";

    public WeatherInfo getWeather(double lat, double lon) {
        Map<String, Integer> grid = GeoUtil.convertToGrid(lat, lon);

        // ✅ baseDate/baseTime 자동 계산 추가
        String baseTime = getBaseTime();
        String baseDate = getBaseDate(baseTime);

        WeatherInfo info = new WeatherInfo();
        
        info.setLatitude(lat);           // ✅ 추가
        info.setLongitude(lon);          // ✅ 추가
        info.setBaseDate(baseDate);      // ✅ 추가
        info.setBaseTime(baseTime);      // ✅ 추가

        try {
            String encodedKey = URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8.name());

            String rawQuery = "serviceKey=" + encodedKey +
                    "&pageNo=1" +
                    "&numOfRows=100" +
                    "&dataType=JSON" +
                    "&base_date=" + baseDate +
                    "&base_time=" + baseTime +
                    "&nx=" + grid.get("nx") +
                    "&ny=" + grid.get("ny");

            String fullUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?" + rawQuery;
            URI uri = URI.create(fullUrl);

            System.out.println("🔥 최종 요청 URI: " + uri);
            RestTemplate restTemplate = new RestTemplate();
            String json = restTemplate.getForObject(uri, String.class);
            System.out.println("📦 응답 JSON: " + json);

            JSONObject root = new JSONObject(json);
            JSONArray items = root.getJSONObject("response")
                    .getJSONObject("body")
                    .getJSONObject("items")
                    .getJSONArray("item");

            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String category = item.getString("category");
                String fcstValue = item.getString("fcstValue");

                switch (category) {
                    case "TMP":
                        info.setTemperature(fcstValue);
                        break;
                    case "SKY":
                        info.setSky(fcstValue);
                        break;
                    case "PTY":
                        info.setRainType(fcstValue);
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ 날씨 API 호출 실패: " + e.getMessage());
            info.setTemperature("에러");
            info.setSky("에러");
            info.setRainType("에러");
        }

        System.out.println("🌤️ 최종 날씨 정보: " + info);
        return info;
    }

    // ✅ 추가: 현재 시각에 맞는 base_time 계산
    private String getBaseTime() {
        LocalTime now = LocalTime.now();

        if (now.isBefore(LocalTime.of(2, 10))) return "2300";
        else if (now.isBefore(LocalTime.of(5, 10))) return "0200";
        else if (now.isBefore(LocalTime.of(8, 10))) return "0500";
        else if (now.isBefore(LocalTime.of(11, 10))) return "0800";
        else if (now.isBefore(LocalTime.of(14, 10))) return "1100";
        else if (now.isBefore(LocalTime.of(17, 10))) return "1400";
        else if (now.isBefore(LocalTime.of(20, 10))) return "1700";
        else if (now.isBefore(LocalTime.of(23, 10))) return "2000";
        else return "2300";
    }

    // ✅ 추가: baseTime이 2300인데 새벽이면 전날 날짜로 보정
    private String getBaseDate(String baseTime) {
        LocalDate today = LocalDate.now();
        if ("2300".equals(baseTime) && LocalTime.now().isBefore(LocalTime.of(2, 10))) {
            return today.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        return today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
