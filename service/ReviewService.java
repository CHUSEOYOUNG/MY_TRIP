package com.sist.web.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.ReviewDao;
import com.sist.web.model.Review;

@Service("reviewService")
public class ReviewService {

	private static Logger logger = LoggerFactory.getLogger(ReviewService.class);
	
	@Autowired
	private ReviewDao reviewDao;
	
	//리뷰 등록
	public int reviewInsert(Review review)
	{
		int count = 0;
		
		try
		{
			count = reviewDao.reviewInsert(review);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewInsert Exception", e);
		}
		
		return count;
	}
	
	//리뷰 있나 조회
	public int reviewCount(Review review)
	{
		int count = 0;
		
		try
		{
			count = reviewDao.reviewCount(review);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewCount Exception", e);
		}
		
		return count;
	}
	
	//리뷰 리스트
	public List<Review> reviewList(String userId)
	{
		List<Review> review = new ArrayList<>();
		
		try
		{
			review = reviewDao.reviewList(userId);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewList Exception", e);
		}
		
		return review;
	}
	
	//숙소페이지리뷰조회
	public List<Review> reviewAccommList(String accommId)
	{
		List<Review> review = new ArrayList<>();
		
		try
		{
			review = reviewDao.reviewAccommList(accommId);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewAccommList Exception", e);
		}
		
		return review;
	}
	//숙소페이지평균별점
	public double reviewRatingAvg(String accommId)
	{
		double rate = 0;
		
		try
		{
			rate = reviewDao.reviewRatingAvg(accommId);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewRatingAvg Exception", e);
		}
		
		return rate;
	}
	//숙소페이지후기갯수
	public int reviewAccommCount(String accommId)
	{
		int count = 0;
		
		try
		{
			count = reviewDao.reviewAccommCount(accommId);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewAccommCount Exception", e);
		}
		
		return count;
	}
	
	//리뷰 삭제
	public int reviewDelete(String accommReviewId)
	{
		int count = 0;
		
		try
		{
			count = reviewDao.reviewDelete(accommReviewId);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewDelete Exception", e);
		}
		
		return count;
	}
	
	//리뷰 셀렉트
	public Review reviewSelect(String accommReviewId)
	{
		Review review = new Review();
		
		try
		{
			review = reviewDao.reviewSelect(accommReviewId);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewSelect Exception", e);
		}
		
		return review;
	}
	
	//리뷰 업데이트
	public int reviewUpdate(String userId)
	{
		int count = 0;
		
		try
		{
			count = reviewDao.reviewUpdate(userId);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewUpdate Exception", e);
		}
		
		return count;
	}
	
	//평균별점업뎃쳐야할숙소아이디리스트
	public List<String> reviewUpdateList(String userId)
	{
		List<String> list = null;
		
		try
		{
			list = reviewDao.reviewUpdateList(userId);
		}
		catch(Exception e)
		{
			logger.error("[ReviewService]reviewUpdateList Exception", e);			
		}
		
		return list;
	}
}
