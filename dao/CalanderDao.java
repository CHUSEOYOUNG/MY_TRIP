package com.sist.web.dao;

import java.util.List;

import com.sist.web.model.Calander;
import com.sist.web.model.CalanderList;
import com.sist.web.model.UserPlace;

public interface CalanderDao {
    void insertCalanderList(CalanderList list);
    void insertCalander(Calander detail);
    List<Calander> getCalanderWithUnifiedSpotName(String listId);  // 정확한 시그니처
    
    CalanderList selectListById(String listId);
    void deleteCalandersByListId(String listId);
    void deleteCalanderListById(String listId);  // 일정 리스트 자체 삭제용
    List<CalanderList> getCalanderListsByUser(String userId);
    void insertManualPlace(UserPlace place);
}
