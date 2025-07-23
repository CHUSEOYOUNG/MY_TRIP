package com.sist.web.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.RecommendDao;
import com.sist.web.model.Recommend;

@Service("recommendService")
public class RecommendService {
	private static Logger logger = LoggerFactory.getLogger(RecommendService.class);
	
	@Autowired
	private RecommendDao recommendDao;
	
	//좋아요를 눌렀는지 조회해줘요
	public int recommendInquiry(Recommend recom)
	{
		int count = 0;
		
		try
		{
			count = recommendDao.recommendInquiry(recom);
		}
		catch(Exception e)
		{
			logger.error("[RecommendService] recommendInquiry : ", e);
		}
		
		return count;
	}
	
	//좋아요
	public int recommendInsert(Recommend recom)
	{
		int count = 0;
		
		try
		{
			count = recommendDao.recommendInsert(recom);
		}
		catch(Exception e)
		{
			logger.error("[RecommendService] recommendInsert : ", e);
		}
		
		return count;
	}
	
	//좋아요취소
	public int recommendDelete(Recommend recom)
	{
		int count = 0;
		
		try
		{
			count = recommendDao.recommendDelete(recom);
		}
		catch(Exception e)
		{
			logger.error("[RecommendService] recommendDelete : ", e);
		}
		
		return count;
	}
	
	//좋아요 LIST
	public List<Integer> recommendList(String userId)
	{
		List<Integer> list = null;
		
		try
		{
			list = recommendDao.recommendList(userId);
		}
		catch(Exception e)
		{
			logger.error("[RecommendService] recommendList : ", e);
		}
		
		return list;
	}
}
