package com.sist.web.dao;

import java.util.List;

import com.sist.web.model.Review;

public interface ReviewDao {

	//리뷰 등록
	public int reviewInsert(Review review);
	//리뷰 있나 조회
	public int reviewCount(Review review);
	//리뷰
	public List<Review> reviewList(String userId);
	//숙소페이지리뷰조회
	public List<Review> reviewAccommList(String accommId);
	//숙소페이지평균별점
	public double reviewRatingAvg(String accommId);
	//숙소페이지후기갯수
	public int reviewAccommCount(String accommId);
	//리뷰 삭제
	public int reviewDelete(String accommReviewId);
	//리뷰 셀렉트
	public Review reviewSelect(String accommReviewId);
	//리뷰 업데이트
	public int reviewUpdate(String userId);
	//평균별점업뎃쳐야할숙소아이디리스트
	public List<String> reviewUpdateList(String userId);
}
