package com.sist.web.dao;

import org.springframework.stereotype.Repository;

import com.sist.web.model.AccommodationRoom;

import java.util.List;
import java.util.Map;

@Repository("AccommodationRoomDao")
public interface AccommodationRoomDao {
    List<String> getAllAccommIds();  // 숙소 ID 목록 (accomm로 통일)
    int existsAccommodationRoom(String accommRoomId);  // 중복 확인
    void insertAccommodationRoom(AccommodationRoom room);  // insert
    Long getNextAccommRoomSeq();  // 시퀀스 nextval
    List<AccommodationRoom> getAllAccommodationRooms();
    List<AccommodationRoom> searchByAccommId(String accommId);
    AccommodationRoom searchByAccommRoomId(String accommRoomId);
    List<AccommodationRoom> getAvailableRoomsByDate(Map<String, Object> param);
    int isRoomOverbooked(Map<String, Object> params);
    int getStandardPrice(String roomId);
}
