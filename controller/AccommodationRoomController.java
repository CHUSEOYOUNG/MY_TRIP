package com.sist.web.controller;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.sist.web.model.AccommodationRoom;
import com.sist.web.service.AccommodationRoomService;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AccommodationRoomController {

    @Autowired
    private AccommodationRoomService accommodationRoomService;

    @GetMapping("/syncAccommodationRoom")
    public String syncAccommodationRoom() {
        int totalCount = 0;
        try {
            List<String> accommIdList = accommodationRoomService.getAllAccommIds();
            if (accommIdList == null) {
                return "숙소 ID 리스트가 null 입니다.";
            }
            if (accommIdList.isEmpty()) {
                return "숙소 ID 리스트가 비어있습니다.";
            }
            for (String accommId : accommIdList) {
                if (accommId == null) {
                    return "숙소 ID 중에 null 값이 있습니다.";
                }
                List<AccommodationRoom> rooms = accommodationRoomService.fetchAccommodationRoom(accommId);
                if (rooms == null) {
                    continue;
                }
                accommodationRoomService.saveAccommodationRoomList(rooms);
                totalCount += rooms.size();
            }
            return "객실 동기화 완료: " + totalCount + "건 저장됨.";
        } catch (Exception e) {
            e.printStackTrace();
            return "객실 동기화 중 에러 발생: " + e.getMessage();
        }
    }

}
