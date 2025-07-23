package com.sist.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.web.dao.AccommodationDao;
import com.sist.web.dao.AccommodationRoomDao;
import com.sist.web.dao.SigunguDao;
import com.sist.web.model.Accommodation;
import com.sist.web.model.AccommodationRoom;
import com.sist.web.model.Sigungu;

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
public class AccommodationService {
	private static Logger logger = LoggerFactory.getLogger(AccommodationService.class);
	
    private static final String BASE_URL = "http://apis.data.go.kr/B551011/KorService2/searchStay2";
    private static final String SERVICE_KEY = "FI/5+Yaw6f0s/3FPHecXtwv8WvGz4xVfTDwKdI9Poe+KV9qTGaG+wGoh2khuWd7w4mUKPGC1dIsyvNORXpkrrQ==";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AccommodationDao accommodationDao;
    @Autowired
    private AccommodationRoomDao accommodationRoomDao;
    @Autowired
    private SigunguDao sigunguDao;  // 시도+시군구 리스트 조회용
    
    
    public List<String> getAllAccommIds() {
        return accommodationRoomDao.getAllAccommIds();
    }
    
    // 전체 수집 시작
    public void syncAllAccommodations() throws Exception {
        List<Sigungu> sigunguList = sigunguDao.getAllSigungu();

        boolean startSync = false;
        String startRegionId = "37";
        String startSigunguId = "13";

        for (Sigungu sigungu : sigunguList) {
            String regionId = sigungu.getRegionId();
            String sigunguId = sigungu.getSigunguId();

            if (!startSync) {
                if (regionId.equals(startRegionId)) {
                    if (sigunguId.equals(startSigunguId)) {
                        startSync = true; // 이 시군구부터 시작
                    } else {
                        continue; // 아직 시작 안함, 건너뜀
                    }
                } else {
                    continue; // 시작 지역 도달 전이므로 건너뜀
                }
            }

            System.out.println("Sync start: regionId=" + regionId + ", sigunguId=" + sigunguId);
            syncAccommodationByRegionSigungu(regionId, sigunguId);
        }
    }
    

    // 한 시도+시군구 조합 숙박 동기화 (페이징 자동 처리)
    public void syncAccommodationByRegionSigungu(String regionId, String sigunguId) throws Exception {
        String rawQuery = "serviceKey=" + URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8.name()) +
                "&MobileOS=ETC" +
                "&MobileApp=MyApp" +
                "&_type=json" +
                "&areaCode=" + regionId +
                "&sigunguCode=" + sigunguId +
                "&pageNo=1" +
                "&numOfRows=1000"; 

        String fullUrl = BASE_URL + "?" + rawQuery;
        URI uri = URI.create(fullUrl);

        String response = restTemplate.getForObject(uri, String.class);
        if (response != null && response.trim().startsWith("<")) {
            throw new RuntimeException("API 호출 실패: " + response);
        }

        JsonNode root = objectMapper.readTree(response).path("response").path("body");
        JsonNode itemsNode = root.path("items").path("item");

        List<Accommodation> pageData = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode node : itemsNode) {
                Accommodation accom = objectMapper.treeToValue(node, Accommodation.class);
                accom.setSellerId("admin");
                accom.setAccomStatus("Y");
                accom.setAccomAvg(0);
                pageData.add(accom);
            }
        }

        saveAccommodationList(pageData);
        System.out.println(regionId + "-" + sigunguId + " 저장 완료, 수집 개수: " + pageData.size());
    }

    public Accommodation fetchAccommodationDescription(String accommId) throws Exception {
        String encodedKey = URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8.name());

        String rawQuery = "serviceKey=" + encodedKey +
		                "&MobileOS=ETC" +
		                "&MobileApp=MyApp" + 
                          "&contentId="  + accommId +
                          "&_type=json";
        String fullUrl = "http://apis.data.go.kr/B551011/KorService2/detailCommon2" + "?" + rawQuery;
        URI uri = URI.create(fullUrl);


        String response = restTemplate.getForObject(uri, String.class);
        if (response == null || response.trim().isEmpty()) {
            return new Accommodation();
        }
        if (response.trim().startsWith("<")) {
            throw new RuntimeException("객실 API 호출 실패: " + response);
        }

        JsonNode itemsNode = objectMapper.readTree(response)
            .path("response")
            .path("body")
            .path("items")
            .path("item");

        if (itemsNode.isArray() && itemsNode.size() > 0) {
            // 배열인 경우 → 첫 번째 요소만 사용
            JsonNode firstItem = itemsNode.get(0);
            return objectMapper.treeToValue(firstItem, Accommodation.class);
        } else if (!itemsNode.isArray()) {
            // 단일 객체인 경우
            return objectMapper.treeToValue(itemsNode, Accommodation.class);
        } else {
            // 데이터 없음
            return new Accommodation();
        }
    }
    
    // 한 페이지 수집
    public List<Accommodation> fetchAccommodation(int pageNo, int numOfRows, String regionId, String sigunguCode) throws Exception {
        String rawQuery = "serviceKey=" + SERVICE_KEY +
                "&MobileOS=ETC" +
                "&MobileApp=MyApp" +
                "&_type=json" +
                "&areaCode=" + regionId +        // ← 추가
                "&sigunguCode=" + sigunguCode +
                "&pageNo=" + pageNo +
                "&numOfRows=" + numOfRows;

        String fullUrl = BASE_URL + "?" + rawQuery;
        URI uri = URI.create(fullUrl);

        System.out.println("요청 URL = " + fullUrl);

        // byte로 받아서 인코딩 처리 (깨짐 방지)
        byte[] responseBytes = restTemplate.getForObject(uri, byte[].class);
        String response = new String(responseBytes, "EUC-KR");

        if (response != null && response.trim().startsWith("<")) {
            throw new RuntimeException("API 호출 실패: " + response);
        }

        JsonNode itemsNode = objectMapper.readTree(response)
                .path("response").path("body").path("items").path("item");

        List<Accommodation> resultList = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode node : itemsNode) {
                Accommodation accom = objectMapper.treeToValue(node, Accommodation.class);
                accom.setSellerId("admin");
                accom.setAccomStatus("Y");
                accom.setAccomAvg(0);
                resultList.add(accom);
            }
        }
        return resultList;
    }
    
    

    public void saveAccommodationList(List<Accommodation> list) {
        for (Accommodation accom : list) {
            accommodationDao.insertAccommodation(accom);
        }
    }
    
    public void updateAccommodationDescription(Accommodation accomm) {
        accommodationDao.updateAccommodationDescription(accomm);
    }

    
    // 기능구현을 위해
    

    

    public List<Accommodation> getAllAccommodations() {
        return accommodationDao.getAllAccommodations(); // DAO에서 전체 조회

    }
    

    public Accommodation selectAccommodation(String accommId) {
    	return accommodationDao.selectAccommodation(accommId);
    }
    public void insertAccommodation(Accommodation accom) {
        // 혹시 accomId가 없다면 직접 생성하거나 UUID 활용 가능
        if (accom.getAccomId() == null || accom.getAccomId().isEmpty()) 
        {
            accom.setAccomId("A_" + System.currentTimeMillis());
        }
    }
    public int insertAccommodationForm(Accommodation accommodation) {
        return accommodationDao.insertAccommodationForm(accommodation);
    }
    @Autowired
    private LikeService likeService; // 이미 주입되어 있을 수도 있음

    public List<Accommodation> findBySigunguList(List<Sigungu> sigungulist, String userId, int page, String keyword) {
    	int pageSize = 20;
    	
    	int start = (page -1) * pageSize;
    	int end = page * pageSize;
    	
    	Map<String, Object> param = new HashMap<>();
    	param.put("start", start);
    	param.put("end", end);
    	param.put("list", sigungulist);
    	param.put("keyword", keyword);
        List<Accommodation> list = accommodationDao.searchBySigungu(param);
        
        if (userId == null || userId.trim().isEmpty()) return list;

        Set<String> likedIds = likeService.getLikedSpotIdSet(userId);
        list.forEach(ac -> ac.setLiked(likedIds.contains(ac.getAccomId())));
        return list;
    }
    
    public int getAccommodationcount(List<Sigungu> sigunguList, String keyword) {
    	Map<String, Object> param = new HashMap<>();
    	param.put("list", sigunguList);
    	param.put("keyword", keyword);
    	return accommodationDao.getAccommodationCount(param);
    }

    public List<Accommodation> findBySellerId(String sellerId) {
        return accommodationDao.findBySellerId(sellerId);
    }
    
    public int accommRateAverage(Accommodation accom)
    {
    	int count = 0;
    	try
    	{
    		count = accommodationDao.accommRateAverage(accom);
    	}
    	catch(Exception e)
    	{
    		logger.error("[AccommodationService]accommRateAverage Exception", e);
    	}
    	return count;
    }
}
