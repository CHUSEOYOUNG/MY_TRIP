package com.sist.web.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.NoticeDao;
import com.sist.web.model.Notice;

@Service("noticeService")
public class NoticeService {

    @Autowired
    private NoticeDao noticeDao;

    public List<Notice> getNoticeList(int startRow, int pageSize) {
        return noticeDao.selectNoticeList(startRow, pageSize);
    }

    public int getNoticeCount() {
        return noticeDao.countNotice();
    }

    // 🔍 검색된 공지사항 개수 조회
    public int getSearchNoticeCount(Map<String, Object> param) {
        return noticeDao.getSearchNoticeCount(param);
    }

    // 🔍 검색된 공지사항 리스트 조회
    public List<Notice> searchNoticeList(Map<String, Object> param) {
        return noticeDao.searchNoticeList(param);
    }
    
    public Notice selectNoticeById(String noticeId) {
    	return noticeDao.selectNoticeById(noticeId);
    
    }
}


