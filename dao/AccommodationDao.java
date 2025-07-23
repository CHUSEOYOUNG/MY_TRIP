package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import com.sist.web.model.Accommodation;
import com.sist.web.model.Sigungu;
import com.sist.web.model.Tour;

public interface AccommodationDao {
    void insertAccommodation(Accommodation accom);
    List<Accommodation> getAllAccommodations();  // 예시용
    void updateAccommodationDescription(Accommodation accomm);
    List<Accommodation> searchBySigungu(Map<String, Object> param);;
    Accommodation selectAccommodation(String accommId);
    int insertAccommodationForm(Accommodation accommodation);
    List<Accommodation> findBySellerId(String sellerId);
    public int accommRateAverage(Accommodation accom);
    int getAccommodationCount(Map<String, Object> param);
}
