package com.sist.web.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.dao.EditorDao;
import com.sist.web.model.Editor;

@Service("editorService")
public class EditorService {
	private static Logger logger = LoggerFactory.getLogger(EditorService.class);
	
	@Autowired
	private EditorDao editorDao;
	
	//게시물등록
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public int editorInsert(Editor editor) throws Exception
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorInsert(editor);
		}
		catch(Exception e)
		{
			logger.error("[EditorService] editorInsert : ", e);
		}
		
		return count;
	}
	
	//게시물갯수
	public int editorListCount(Editor editor)
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorListCount(editor);
		}
		catch(Exception e)
		{
			logger.error("[EditorService] editorListCount : ", e);
		}
		
		return count;
	}
	
	//게시물리스트
	public List<Editor> editorList(Editor editor)
	{
		List<Editor> list = null;
		
		try
		{
			list = editorDao.editorList(editor);
		}
		catch(Exception e)
		{
			logger.error("[EditorService] editorList Exception : ", e);
		}
		
		return list;
	}
	
	//게시물 조회
	public Editor editorSelect(int planId)
	{
		Editor editor = null;
		
		try
		{
			editor = editorDao.editorSelect(planId);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorSelect Exception", e);
		}
		
		return editor;
	}
	
	//게시물 수정
	public int editorUpdate(Editor editor)
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorUpdate(editor);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorUpdate Exception", e);
		}
		
		return count;
	}
	
	//게시글 삭제
	public int editorDelete(int planId)
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorDelete(planId);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorDelete Exception", e);
		}
		
		return count;
	}
	
	//게시물 썸네일
	public String editorThumbnail(int planId)
	{
		String thumbnail = "";
		
		try
		{
			thumbnail = editorDao.editorThumbnail(planId);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorThumbnail Exception", e);
		}
		
		return thumbnail;
	}
	
	//게시물 조회수증가
	public int editorCountUpdate(int planId)
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorCountUpdate(planId);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorCountUpdate Exception", e);
		}
		
		return count;
	}
	//좋아요증가
	public int editorLikeIncre(int planId)
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorLikeIncre(planId);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorLikeIncre Exception", e);
		}
		
		return count;
	}
	//좋아요감소
	public int editorLikeDecre(int planId)
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorLikeDecre(planId);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorLikeDecre Exception", e);
		}
		
		return count;
	}
	
	//일정에대한후기체크
	public int editorScheduleChk(Editor editor)
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorScheduleChk(editor);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorScheduleChk Exception", e);
		}
		
		return count;
	}
	
	//캘린더아이디로플랜아이디조회
	public int editorPlanId(Editor editor)
	{
		int count = 0;
		
		try
		{
			count = editorDao.editorPlanId(editor);
		}
		catch(Exception e)
		{
			logger.error("[EditorService]editorPlanId Exception", e);
		}
		
		return count;
	}
	
	//내게시글조회
	public List<Editor> editorMyplan(String userId)
	{
		 List<Editor> list = null;
		 
		 try
		 {
			 list = editorDao.editorMyplan(userId);
		 }
		 catch(Exception e)
		 {
			 logger.error("[EditorService]editorMyplan Exception", e);
		 }
		 
		 return list;
	}
	
	//탈퇴시게시글비공개
	public int editorStatus(String userId)
	{
		int count = 0;
		
		 try
		 {
			 count = editorDao.editorStatus(userId);
		 }
		 catch(Exception e)
		 {
			 logger.error("[EditorService]editorStatus Exception", e);
		 }
		 
		 return count;
	}
	
	//게시글 신고수 증가
	public int editorReport(int planId)
	{
		int count = 0;
		
		 try
		 {
			 count = editorDao.editorReport(planId);
		 }
		 catch(Exception e)
		 {
			 logger.error("[EditorService]editorReport Exception", e);
		 }
		 
		 return count;
	}
	
	//게시글 베스트 리뷰
	public List<Editor> getBestReviews() {
	    List<Editor> list = null;
	    try {
	        list = editorDao.getBestReviews(); // DAO에서 새로 만들기
	    } catch (Exception e) {
	        logger.error("[EditorService] getBestReviews Exception", e);
	    }
	    return list;
	}

}
