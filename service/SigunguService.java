package com.sist.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.web.dao.SigunguDao;
import com.sist.web.model.Sigungu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SigunguService {
    private static final String BASE_URL = "http://apis.data.go.kr/B551011/KorService2/areaCode2";
    private static final String SERVICE_KEY =
            "FI/5+Yaw6f0s/3FPHecXtwv8WvGz4xVfTDwKdI9Poe+KV9qTGaG+wGoh2khuWd7w4mUKPGC1dIsyvNORXpkrrQ==";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SigunguDao sigunguDao;

    public List<Sigungu> fetchSigunguByRegion(String regionId) throws Exception {
        String encodedKey = URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8.name());
        List<Sigungu> resultList = new ArrayList<>();
        int pageNo = 1;
        int totalCount = 0;

        do {
            String rawQuery = "serviceKey=" + encodedKey +
                              "&MobileOS=ETC" +
                              "&MobileApp=MyApp" +
                              "&_type=json" +
                              "&areaCode=" + regionId +
                              "&numOfRows=100" +
                              "&pageNo=" + pageNo;

            String fullUrl = BASE_URL + "?" + rawQuery;
            URI uri = URI.create(fullUrl);

            System.out.println("Sigungu API 요청 URL = " + fullUrl);

            String response = restTemplate.getForObject(uri, String.class);

            if (response != null && response.trim().startsWith("<")) {
                throw new RuntimeException("Sigungu API 호출 실패: " + response);
            }

            JsonNode root = objectMapper.readTree(response).path("response").path("body");
            JsonNode itemsNode = root.path("items").path("item");
            totalCount = root.path("totalCount").asInt();

            if (itemsNode.isArray()) {
                for (JsonNode node : itemsNode) {
                    Sigungu sigungu = new Sigungu();
                    sigungu.setRegionId(regionId); // 상위 지역코드 (시도)
                    sigungu.setSigunguId(node.path("code").asText());  // 시군구 코드
                    sigungu.setSigunguName(node.path("name").asText());
                    sigungu.setSigunguLat("0");
                    sigungu.setSigunguLon("0");
                    resultList.add(sigungu);
                }
            }
            pageNo++;
        } while ((pageNo - 1) * 100 < totalCount);

        return resultList;
    }

    public void syncSigunguForAllRegions(List<String> regionIdList) throws Exception {
        for (String regionId : regionIdList) {
            List<Sigungu> sigunguList = fetchSigunguByRegion(regionId);
            saveSigunguList(sigunguList);
        }
    }

    public void saveSigunguList(List<Sigungu> list) {
        for (Sigungu sigungu : list) {
            sigunguDao.insertSigungu(sigungu);
        }
    }
    
    public List<Sigungu> getAllSigungus() {
        return sigunguDao.getAllSigungu();
    }
    
    //시군구코드조회 추가
    public Sigungu sigunguSelect(String regionId, String sigunguName)
    {
    	Sigungu sigungu = sigunguDao.sigunguSelect(regionId, sigunguName);
    	
    	return sigungu;
    }
}
