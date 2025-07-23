package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sist.web.model.Like;

@Mapper
public interface LikeDao {
    
    // 찜 추가
    int insertLike(Like like);
    
    // 찜 삭제
    
    // 찜 상태 확인
    int checkLike(@Param("userId") String userId, @Param("spotId") String spotId);
    
    // 사용자별 찜 목록 조회
    
    // 특정 spot의 찜 개수
    int countBySpotId(String spotId);
    List<Like> findByUserId(@Param("userId") String userId);
    List<Like> findByUserIdWithDetail(@Param("userId") String userId);    
    void insertAccommLike(@Param("userId") String userId, @Param("spotId") String spotId);
    void deleteAccommLike(@Param("userId") String userId, @Param("spotId") String spotId);
    boolean isAccommLiked(@Param("userId") String userId, @Param("spotId") String spotId);
    int getAccommLikeCount(@Param("spotId") String spotId);
    List<Like> findAccommLikeListByUser(String userId);
    void deleteLike(@Param("userId") String userId, @Param("spotId") String spotId);


}
