package com.sist.web.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sist.web.dao.LikeDao;
import com.sist.web.model.Like;

@Service
public class LikeService {

        @Autowired
        private LikeDao likeDao;
        
        // 찜 토글 (있으면 삭제, 없으면 추가)
        @Transactional
        public boolean toggleLike(String userId, String spotId) {
            if (isLiked(userId, spotId)) {
                likeDao.deleteLike(userId, spotId);
                return false;            // ➜ 해제 후 false
            } else {
                Like like = new Like(userId, spotId);
                likeDao.insertLike(like);
                return true;             // ➜ 추가 후 true
            }
        }
        
        // 찜 상태 확인
        public boolean isLiked(String userId, String spotId) {
            return likeDao.checkLike(userId, spotId) > 0;
        }
        
        // 사용자별 찜 목록
        public List<Like> getUserLikes(String userId) {
            return likeDao.findByUserId(userId);
        }
        
        // 특정 관광지의 찜 개수
        public int getLikeCount(String spotId) {
            return likeDao.countBySpotId(spotId);
        }
        public List<Like> getUserLikesWithDetail(String userId) {
            return likeDao.findByUserIdWithDetail(userId);
        }
        
        public boolean toggleAccommLike(String userId, String spotId) {
            if (likeDao.isAccommLiked(userId, spotId)) {
                likeDao.deleteAccommLike(userId, spotId);
                return false;
            } else {
                likeDao.insertAccommLike(userId, spotId);
                return true;
            }
        }
        public void deleteLike(String userId, String spotId) {
            likeDao.deleteLike(userId, spotId);
        }
        
        public boolean isAccommLiked(String userId, String spotId) {
            return likeDao.isAccommLiked(userId, spotId);
        }

        
        public int getAccommLikeCount(String spotId) {
            return likeDao.getAccommLikeCount(spotId);
        }
        public List<Like> getAccommLikesByUser(String userId) {
            return likeDao.findAccommLikeListByUser(userId);
        }
        public Set<String> getLikedSpotIdSet(String userId) {
            List<Like> likes = likeDao.findByUserId(userId);
            if (likes == null) return Collections.emptySet();

            return likes.stream()
                        .filter(Objects::nonNull)
                        .map(Like::getSpotId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
        }
    }