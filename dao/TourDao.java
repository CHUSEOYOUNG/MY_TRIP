package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import com.sist.web.model.Sigungu;
import com.sist.web.model.Tour;

public interface TourDao {
	void insertTour(Tour tour);
	List<Tour> getAllTours();
	List<Tour> searchBySigungu(Map<String, Object> param);
	int getTourCount(Map<String, Object> param);
}
