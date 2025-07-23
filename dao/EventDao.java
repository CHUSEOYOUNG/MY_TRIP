package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.sist.web.model.Event;

@Repository("eventDao")
public interface EventDao {
	
	public Event selectEventById(String eventId);
	
	List<Event> selectActiveEvents();
    List<Event> selectEndedEvents();
    
    int countEvent();
	
    void increaseEventCount(String eventId);
    
    
    // 공지 이벤트 1개 조회
    public Event selectNoticeEvent();

    // 페이징된 일반 이벤트 목록
    List<Event> selectEventListPaging(@Param("startRow") int startRow, @Param("pageSize") int pageSize);

    // 전체 이벤트 개수
    int countAllEvents();
    
    
    
    // 검색 이벤트 개수
    int getSearchEventCount(Map<String, Object> param);
    List<Event> searchEventList(Map<String, Object> param);
    
    // 진행 중 이벤트 페이징 조회
    public List<Event> selectActiveEventsByPage(@Param("startRow") int startRow, @Param("pageSize") int pageSize);

    // 전체 개수 조회 (진행 중 이벤트만)
    public int countActiveEvents();

}
