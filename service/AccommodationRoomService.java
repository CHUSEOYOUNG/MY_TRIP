package com.sist.web.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sist.web.dao.AccommodationRoomDao;
import com.sist.web.model.AccommodationRoom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class AccommodationRoomService {

    @Autowired
    private AccommodationRoomDao accommodationRoomDao;



    public List<String> getAllAccommIds() {
        return accommodationRoomDao.getAllAccommIds();
    }

    private static final String ROOM_BASE_URL = "http://apis.data.go.kr/B551011/KorService2/detailInfo2";
    private static final String SERVICE_KEY =
        "MHbE44hd7kHCOFVk5VucNc1XiPWTzxPAraI2RBlclEk8DFoZtBZWPX1gTMSVJ5j1U6ggq3bD6eViUAyxJfBpdQ==";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();



    public List<AccommodationRoom> fetchAccommodationRoom(String accommId) throws Exception {
        String encodedKey = URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8.name());

        String rawQuery = "serviceKey=" + encodedKey +
		                "&MobileOS=ETC" +
		                "&MobileApp=MyApp" +
		                "&contentTypeId=32" + 
                          "&contentId="  + accommId +
                          "&_type=json";
        String fullUrl = ROOM_BASE_URL + "?" + rawQuery;
        URI uri = URI.create(fullUrl);


        String response = restTemplate.getForObject(uri, String.class);
        if (response == null || response.trim().isEmpty()) {
            return new ArrayList<>();
        }
        if (response.trim().startsWith("<")) {
            throw new RuntimeException("객실 API 호출 실패: " + response);
        }

        JsonNode itemsNode = objectMapper.readTree(response)
            .path("response")
            .path("body")
            .path("items")
            .path("item");

        List<AccommodationRoom> rooms = new ArrayList<>();
        if (itemsNode.isArray()) {
            for (JsonNode node : itemsNode) {
                AccommodationRoom room = objectMapper.treeToValue(node, AccommodationRoom.class);
                rooms.add(room);
            }
        }
        return rooms;
    }


    public boolean existsAccommodationRoom(String accommRoomId) {
        int count = accommodationRoomDao.existsAccommodationRoom(accommRoomId);
        return count > 0;
    }


    public void saveAccommodationRoom(AccommodationRoom room) {
        accommodationRoomDao.insertAccommodationRoom(room);
    }


    public String generateAccommRoomId() {
        Long nextVal = accommodationRoomDao.getNextAccommRoomSeq();
        return "R_" + nextVal;
    }


    @Transactional
    public void saveAccommodationRoomList(List<AccommodationRoom> roomList) {
        if (roomList == null || roomList.isEmpty()) {
            return;
        }

        for (AccommodationRoom room : roomList) {
            if (room == null) {
                continue;  // null 방어
            }

            // 객실 ID가 없으면 새로 생성 후 설정
            if (room.getAccommRoomId() == null || room.getAccommRoomId().isEmpty()) {
                String newId = generateAccommRoomId();
                room.setAccommRoomId(newId);
            }

            // DB에 없는 경우에만 저장
            if (!existsAccommodationRoom(room.getAccommRoomId())) {
                saveAccommodationRoom(room);
            }
        }
    }
    
    public List<AccommodationRoom> searchByAccommid(String accommId){
    	return accommodationRoomDao.searchByAccommId(accommId);
    }
    
    public AccommodationRoom searchByAccommRoomId(String accommRoomId) {
    	return accommodationRoomDao.searchByAccommRoomId(accommRoomId);
    }
    
    public List<AccommodationRoom> getAvailableRoomsByDate(String accommId, String checkIn, String checkOut) {
        // 파라미터를 Map으로 구성해서 Dao에 전달
        Map<String, Object> param = new HashMap<>();
        param.put("accommId", accommId);
        param.put("checkIn", checkIn);
        param.put("checkOut", checkOut);
        System.out.println(">>> param map: " + param);
        return accommodationRoomDao.getAvailableRoomsByDate(param);

    }
}
