package com.sist.web.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.CalanderDao;
import com.sist.web.model.Calander;
import com.sist.web.model.CalanderList;
import com.sist.web.model.UserPlace;

import org.springframework.transaction.annotation.Transactional;

@Service
public class CalanderService {

    @Autowired
    private CalanderDao calanderDao;

    public void saveList(CalanderList list) {
        calanderDao.insertCalanderList(list);
    }

    @Transactional
    public void saveDetail(Calander detail) {
        calanderDao.insertCalander(detail);
    }

    public List<Calander> getCalanders(String listId) {
        return calanderDao.getCalanderWithUnifiedSpotName(listId);
    }
    public CalanderList getListById(String listId) {
        return calanderDao.selectListById(listId);
    }

    public void deleteDetailsByListId(String listId) {
        calanderDao.deleteCalandersByListId(listId);
    }
    public void deleteListById(String listId) {
        calanderDao.deleteCalanderListById(listId);
    }
    public List<CalanderList> getListsByUser(String userId){
        return calanderDao.getCalanderListsByUser(userId);
    }
    public void saveManualPlace(UserPlace place) {
        calanderDao.insertManualPlace(place);
    }
}

