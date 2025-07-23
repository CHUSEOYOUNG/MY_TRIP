package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import com.sist.web.model.AccommodationRoom;

public interface RoomManageDao {
    void insertAccommodationRoom(AccommodationRoom room);  // 매퍼의 실제 메서드명과 일치
    List<AccommodationRoom> findByAccommId(String accommId);
    AccommodationRoom findByRoomId(String roomId);
    String getNextAccommRoomSeq();
    void deleteRoomById(String roomId);
    int isDuplicateRoomName(Map<String, Object> param);

}
