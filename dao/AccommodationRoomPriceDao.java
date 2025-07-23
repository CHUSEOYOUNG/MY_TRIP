package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import com.sist.web.model.AccommodationRoomPrice;
import com.sist.web.model.RoomPriceResult;

public interface AccommodationRoomPriceDao {
	void insertAccommodationRoomPrice(AccommodationRoomPrice roomprice);
	AccommodationRoomPrice selectAccommodationRoomPrice(Map<String, Object> param);
	RoomPriceResult calculateTotalPrice(String roomId, String checkIn, String checkOut);
	List<AccommodationRoomPrice> selectByAccommRoomId(String roomId);
	int checkDateOverlap(Map<String, Object> param);
	void deleteRoomPrice(String priceId);
}
