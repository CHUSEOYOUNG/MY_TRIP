package com.sist.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.web.dao.SigunguDao;
import com.sist.web.dao.TourDao;
import com.sist.web.model.Accommodation;
import com.sist.web.model.Sigungu;
import com.sist.web.model.Tour;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TourService {
    private static final String BASE_URL = "http://apis.data.go.kr/B551011/KorService2/areaBasedList2";
    private static final String SERVICE_KEY =
            "FI/5+Yaw6f0s/3FPHecXtwv8WvGz4xVfTDwKdI9Poe+KV9qTGaG+wGoh2khuWd7w4mUKPGC1dIsyvNORXpkrrQ==";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    Logger logger = LoggerFactory.getLogger(TourService.class);

    @Autowired
    private TourDao tourDao;

    @Autowired
    private SigunguDao sigunguDao;
    @Autowired
    private LikeService likeService;
    public void syncAllTours() throws Exception {
        List<Sigungu> sigunguList = sigunguDao.getAllSigungu();

        boolean startSync = false;
        String startRegionId = "3";
        String startSigunguId = "3";

        for (Sigungu sigungu : sigunguList) {
            String regionId = sigungu.getRegionId();
            String sigunguId = sigungu.getSigunguId();

            if (!startSync) {
                if (regionId.equals(startRegionId)) {
                    if (sigunguId.equals(startSigunguId)) {
                        startSync = true;
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }

            logger.info("Sync start: regionId={}, sigunguId={}", regionId, sigunguId);
            syncTourByRegionSigungu(regionId, sigunguId);
        }
    }

    public void syncTourByRegionSigungu(String regionId, String sigunguId) throws Exception {
        String encodedKey = URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8.name());

        String rawQuery = "serviceKey=" + encodedKey +
                "&MobileOS=ETC" +
                "&MobileApp=MyApp" +
                "&_type=json" +
                "&areaCode=" + regionId +
                "&sigunguCode=" + sigunguId +
                "&pageNo=1" +
                "&numOfRows=20";

        String fullUrl = BASE_URL + "?" + rawQuery;
        URI uri = URI.create(fullUrl);

        String response = restTemplate.getForObject(uri, String.class);
        if (response != null && response.trim().startsWith("<")) {
            throw new RuntimeException("API 호출 실패: " + response);
        }

        JsonNode itemsNode = objectMapper.readTree(response)
                .path("response").path("body").path("items").path("item");

        List<Tour> tourList = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode node : itemsNode) {
                Tour tour = objectMapper.treeToValue(node, Tour.class);
                tourList.add(tour);
            }
        }

        saveTourList(tourList);
        logger.info("{}-{} 저장 완료, 수집 개수: {}", regionId, sigunguId, tourList.size());
    }

    public void saveTourList(List<Tour> list) {
        for (Tour tour : list) {
            tourDao.insertTour(tour);
        }
    }
    
    public List<Tour> getAllTours() {
        return tourDao.getAllTours();
    }


    public List<Tour> findBySigunguList(List<Sigungu> sigunguList, String userId, int page) {
    	int pageSize = 20;

    	
    	int start = (page - 1) * pageSize; // 0
    	int end = page * pageSize;         // 20
    	
    	Map<String, Object> param = new HashMap<>();
    	param.put("start", start);
    	param.put("end", end);
    	param.put("list", sigunguList);

    	List<Tour> list = tourDao.searchBySigungu(param);

        // 로그인 안 한 경우 그대로 반환
        if (userId == null || userId.trim().isEmpty()) return list;

        Set<String> likedIds = likeService.getLikedSpotIdSet(userId);

        list.forEach(t -> t.setLiked(likedIds.contains(t.getTourId())));
        return list;
    }
    
    public int getTourCount(List<Sigungu> sigunguList) {
        Map<String, Object> param = new HashMap<>();
        param.put("list", sigunguList);
        return tourDao.getTourCount(param);
    }

}
