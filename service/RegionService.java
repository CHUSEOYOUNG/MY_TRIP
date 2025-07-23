package com.sist.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.web.dao.RegionDao;
import com.sist.web.model.Region;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class RegionService {
    private static final String BASE_URL = "http://apis.data.go.kr/B551011/KorService2/areaCode2"; // 예시 URL, 실제 지역 코드 API URL로 변경 필요
    private static final String SERVICE_KEY =
            "FI/5+Yaw6f0s/3FPHecXtwv8WvGz4xVfTDwKdI9Poe+KV9qTGaG+wGoh2khuWd7w4mUKPGC1dIsyvNORXpkrrQ==";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RegionDao regionDao;

    public List<Region> fetchRegions(int pageNo, int numOfRows) throws Exception {
        String encodedKey = URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8.name());

        String rawQuery = "serviceKey=" + encodedKey +
                          "&MobileOS=ETC" +
                          "&MobileApp=MyApp" +
                          "&_type=json" +

                           "&pageNo=" + pageNo +
                          "&numOfRows=" + numOfRows;

        String fullUrl = BASE_URL + "?" + rawQuery;
        URI uri = URI.create(fullUrl);

        System.out.println("Region API 요청 URL = " + fullUrl);

        String response = restTemplate.getForObject(uri, String.class);

        if (response != null && response.trim().startsWith("<")) {
            throw new RuntimeException("Region API 호출 실패: " + response);
        }

        JsonNode itemsNode = objectMapper.readTree(response)
                .path("response")
                .path("body")
                .path("items")
                .path("item");

        List<Region> resultList = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode node : itemsNode) {
                Region region = objectMapper.treeToValue(node, Region.class);
                region.setRegionLon("0");
                region.setRegionLat("0");
                resultList.add(region);
            }
        }
        return resultList;
    }
    
    
    public void syncAllRegions() throws Exception {
        // 1. 시도 먼저 수집
        List<Region> topRegions = fetchRegions(1, 100);
        saveRegionList(topRegions);

        // 2. 시군구 반복 수집
        for (Region topRegion : topRegions) {
            int areaCode = Integer.parseInt(topRegion.getRegionId());
            List<Region> subRegions = fetchSubRegions(areaCode);
            saveRegionList(subRegions);
        }
    }

    

    public List<Region> fetchSubRegions(int areaCode) throws Exception {
    	String encodedKey = URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8.name());
        List<Region> resultList = new ArrayList<>();
        int pageNo = 1;
        int totalCount = 0;

        do {
            String rawQuery = "serviceKey=" + encodedKey +
                              "&MobileOS=ETC" +
                              "&MobileApp=MyApp" +
                              "&_type=json" +
                              "&areaCode=" + areaCode +
                              "&numOfRows=100" +
                              "&pageNo=" + pageNo;

            String fullUrl = BASE_URL + "?" + rawQuery;
            URI uri = URI.create(fullUrl);

            String response = restTemplate.getForObject(uri, String.class);

            if (response != null && response.trim().startsWith("<")) {
                throw new RuntimeException("시군구 API 호출 실패: " + response);
            }

            JsonNode root = objectMapper.readTree(response).path("response").path("body");
            JsonNode itemsNode = root.path("items").path("item");
            totalCount = root.path("totalCount").asInt();

            if (itemsNode.isArray()) {
                for (JsonNode node : itemsNode) {
                    Region region = new Region();
                    region.setRegionId(node.path("code").asText());
                    region.setRegionName(node.path("name").asText());
                    region.setRegionLon("0");
                    region.setRegionLat("0");
                    resultList.add(region);
                }
            }
            pageNo++;
        } while ((pageNo - 1) * 100 < totalCount);

        return resultList;
    }



    

    public void saveRegionList(List<Region> list) {
        for (Region region : list) {
            regionDao.insertRegion(region);
        }
    }
    
    public List<Region> getAllRegions(){
    	return regionDao.getAllRegions();
    }
    
    //지역코드조회 추가
    public Region regionSelect(String regionName)
    {
    	Region region = regionDao.regionSelect(regionName);
    	
    	return region;
    	
    }
}
