/**
 * <pre>
 * 프로젝트명 : HiBoard
 * 패키지명   : com.icia.web.controller
 * 파일명     : IndexController.java
 * 작성일     : 2021. 1. 21.
 * 작성자     : daekk
 * </pre>
 */
package com.sist.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.sist.web.service.EditorService;
import com.sist.web.service.RegionService;
import com.sist.web.service.UserService;
import com.sist.web.model.Editor;
import com.sist.web.model.Region;
import com.sist.web.model.User;
import com.sist.web.util.CookieUtil;

/**
 * <pre>
 * 패키지명   : com.icia.web.controller
 * 파일명     : IndexController.java
 * 작성일     : 2021. 1. 21.
 * 작성자     : daekk
 * 설명       : 인덱스 컨트롤러
 * </pre>
 */
@Controller("indexController")
public class IndexController
{
	private static Logger logger = LoggerFactory.getLogger(IndexController.class);

	/**
	 * <pre>
	 * 메소드명   : index
	 * 작성일     : 2021. 1. 21.
	 * 작성자     : daekk
	 * 설명       : 인덱스 페이지 
	 * </pre>
	 * @param request  HttpServletRequest
	 * @param response HttpServletResponse
	 * @return String
	 */
    @Autowired
    private RegionService regionService;
    @Autowired
    private UserService userService;
    @Autowired
    private EditorService editorService;
    
	@RequestMapping(value = "/index", method=RequestMethod.GET)
	public String index(HttpServletRequest request, HttpServletResponse response, Model model)
	{
        List<Region> regionList = regionService.getAllRegions();
        String thumbnail = "";
        User user = new User();
        String userName = "";
        
        // 2. 베스트 후기 3개 조회
        List<Editor> bestReviewList = editorService.getBestReviews();

        for(Editor editor : bestReviewList)
        {
            thumbnail = editorService.editorThumbnail(Integer.parseInt(editor.getPlanId()));
            editor.setThumbnail(thumbnail);
            user = userService.userSelect(editor.getUserId());
            userName = user.getUserName();
            editor.setUserName(userName);
        }
        
        // 3. 모델에 담기
        model.addAttribute("regionList", regionList);
        model.addAttribute("bestReviewList", bestReviewList);

		return "/index";
	}
	@RequestMapping(value = "/walkthrough", method=RequestMethod.GET)
	public String walkthrough(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/walkthrough";
	}
	
}
