package com.sist.web.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import com.sist.web.model.Like;
import com.sist.web.service.LikeService;

@Controller
@RequestMapping("/like")
public class LikeController {

    @Autowired
    private LikeService likeService;

    // 관광지 찜 토글
    @PostMapping("/toggle")
    @ResponseBody
    public Map<String, Object> toggleLike(@RequestParam("spotId") String spotId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            boolean isLiked = likeService.toggleLike(userId, spotId);
            int likeCount = likeService.getLikeCount(spotId);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", likeCount);
            response.put("message", isLiked ? "찜 추가되었습니다." : "찜 해제되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "오류가 발생했습니다.");
        }

        return response;
    }

    // 관광지 찜 상태 확인
    @GetMapping("/check")
    @ResponseBody
    public Map<String, Object> checkLike(@RequestParam("spotId") String spotId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            response.put("isLiked", false);
            response.put("likeCount", likeService.getLikeCount(spotId));
        } else {
            response.put("isLiked", likeService.isLiked(userId, spotId));
            response.put("likeCount", likeService.getLikeCount(spotId));
        }

        return response;
    }

    // 관광지 찜 ID 목록 (T_로 시작하는 것만)
    @GetMapping("/spotIds")
    @ResponseBody
    public List<String> getLikedTourSpotIds(HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        System.out.println("유저아이디 세션확인" + userId);
        

        if (userId == null) return Collections.emptyList();


        List<Like> likes = likeService.getUserLikes(userId);
        System.out.println("조회된 Like 객체 수: " + (likes != null ? likes.size() : "null"));
        for (Like like : likes) {
            System.out.println("like: " + like);
            System.out.println("  spotId: " + like.getSpotId());
        }


        return likeService.getUserLikes(userId).stream()
                .filter(Objects::nonNull)

                .map(Like::getSpotId)

                .filter(Objects::nonNull) // ✅ spotId가 null인지 체크 // 관광지 찜만 필터

                .filter(Objects::nonNull)

                .collect(Collectors.toList());
    }

    // 숙소 찜 토글
    @PostMapping("/accomm/toggle")
    @ResponseBody
    public Map<String, Object> toggleAccommLike(@RequestParam("spotId") String spotId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return response;
        }

        try {
            boolean isLiked = likeService.toggleAccommLike(userId, spotId);
            int likeCount = likeService.getAccommLikeCount(spotId);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("likeCount", likeCount);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "오류 발생");
        }

        return response;
    }

    // 숙소 찜 상태 확인
    @GetMapping("/accomm/check")
    @ResponseBody
    public Map<String, Object> checkAccommLike(@RequestParam("spotId") String spotId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            response.put("isLiked", false);
            response.put("likeCount", 0);
            return response;
        }

        response.put("isLiked", likeService.isAccommLiked(userId, spotId));
        response.put("likeCount", likeService.getAccommLikeCount(spotId));
        return response;
    }

    // 찜 목록 페이지 (관광지 + 숙소)
    @GetMapping("/list")
    public String likeList(HttpSession session, ModelMap model) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return "redirect:/user/login"; // 로그인 필요

        List<Like> likedTourList = likeService.getUserLikesWithDetail(userId);
        model.addAttribute("likedTourList", likedTourList);

        List<Like> likedAccommList = likeService.getAccommLikesByUser(userId);
        model.addAttribute("likedAccommList", likedAccommList);

        return "/like/list";
    }

    // 숙소 찜 ID 목록 (A_로 시작하는 것만)
    @GetMapping("/accommIds")
    @ResponseBody
    public List<String> getLikedAccommIds(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return Collections.emptyList();

        return likeService.getUserLikes(userId).stream()
        	    .filter(Objects::nonNull)
        	    .map(Like::getSpotId)
        	    .filter(Objects::nonNull)
        	    .collect(Collectors.toList());
    }
    @PostMapping("/delete")
    @ResponseBody
    public Map<String, Object> deleteLike(@RequestParam("spotId") String spotId,
                                          HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        String userId = (String) session.getAttribute("userId");

        if (userId == null || userId.trim().isEmpty()) {
            map.put("success", false);
            map.put("message", "로그인이 필요합니다.");
            return map;
        }

        try {
            likeService.deleteLike(userId, spotId);
            map.put("success", true);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("success", false);
            map.put("message", e.getMessage());
        }
        return map;
    }

}
