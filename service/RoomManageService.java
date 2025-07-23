package com.sist.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.RoomManageDao;
import com.sist.web.model.AccommodationRoom;

@Service
public class RoomManageService {
      @Autowired
       private RoomManageDao roomManageDao;
       
       public void insertRoom(AccommodationRoom room) {
           String nextSeq = roomManageDao.getNextAccommRoomSeq();
           room.setAccommRoomId(nextSeq);  // 시퀀스에서 이미 'R_000001' 형태로 반환됨
           roomManageDao.insertAccommodationRoom(room);  // 매퍼의 실제 메서드명과 일치
       }
       
       public List<AccommodationRoom> findRoomsByAccomm(String accommId) {
           return roomManageDao.findByAccommId(accommId);  // 매퍼에 실제 존재하는 메서드 호출
       }
       
       public AccommodationRoom getRoom(String roomId) {
           return roomManageDao.findByRoomId(roomId);
       }
       public void deleteRoom(String roomId) {
           roomManageDao.deleteRoomById(roomId);
       }
       public boolean isRoomNameDuplicate(String accommId, String roomName) {
           Map<String, Object> param = new HashMap<>();
           param.put("accommId", accommId);
           param.put("roomName", roomName);
           return roomManageDao.isDuplicateRoomName(param) > 0;
       }
}
