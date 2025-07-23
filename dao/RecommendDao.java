package com.sist.web.dao;

import java.util.List;

import com.sist.web.model.Recommend;

public interface RecommendDao {
	
	//좋아요를 눌렀나 조회해줘요
	public int recommendInquiry(Recommend recom);
	//좋아요 INSERT
	public int recommendInsert(Recommend recom);
	//좋아요 DELETE
	public int recommendDelete(Recommend recom);
	//좋아요 LIST
	public List<Integer> recommendList(String userId);
}
