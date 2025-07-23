package com.sist.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sist.web.model.Accommodation;
import com.sist.web.model.Response;
import com.sist.web.model.Review;
import com.sist.web.service.AccommodationService;
import com.sist.web.service.ReviewService;
import com.sist.web.util.HttpUtil;

@Controller("reviewController")
public class ReviewController {

	private static Logger logger = LoggerFactory.getLogger(ReviewController.class);
	
	@Autowired
	private ReviewService reviewService;
	@Autowired
	private AccommodationService accomService;
	
	@RequestMapping(value = "/mypage/reviewPopup", method=RequestMethod.GET)
	public String reportPopup(HttpServletRequest request, HttpServletResponse response, ModelMap model)
	{
		String accommId = request.getParameter("accommId");
	    model.addAttribute("accommId", accommId);
	    
	    return "/mypage/reviewPopup";
	}
	
	//리뷰 등록 ajax
	@RequestMapping(value="/mypage/review", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> review(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String accommId = HttpUtil.get(request, "accommId", "");
		String userId = HttpUtil.get(request, "userId", "");
		
		int rating = HttpUtil.get(request, "rating", 0);
		String content = HttpUtil.get(request, "content", "");
		
		String orderId = HttpUtil.get(request, "orderId", "");
		
		try
		{
			Review review = new Review();
			
			review.setAccommId(accommId);
			review.setUserId(userId);
			review.setAccommReviewRating(rating);
			review.setAccommReviewContent(content);
			review.setOrderId(orderId);
			
			if(reviewService.reviewCount(review)>0)
			{
				ajaxResponse.setResponse(-10, "review already exsist");
			}
			else
			{
				if(reviewService.reviewInsert(review)>0)
				{
					double rateAvg = reviewService.reviewRatingAvg(accommId);
					Accommodation accom = new Accommodation();
					accom.setAccomId(accommId);
					accom.setAccomAvg(rateAvg);
					if(accomService.accommRateAverage(accom)>0)
					{
						ajaxResponse.setResponse(0, "review insert success");
					}
				}
				else
				{
					ajaxResponse.setResponse(-1, "review insert fail");
				}
			}
		}
		catch(Exception e)
		{
			ajaxResponse.setResponse(500, "internal server error");
		}
		
		return ajaxResponse;
	}
	
	//리뷰목록
	@RequestMapping(value = "/mypage/reviewlist", method=RequestMethod.GET)
	public String reviewList(HttpServletRequest request, HttpServletResponse response, ModelMap model)
	{
		String userId = (String) request.getSession().getAttribute("userId");
	    List<Review> list = new ArrayList<>();
	    String accomName = "";
	    
	    list = reviewService.reviewList(userId);
	    
	    for (Review review : list)
	    {
	    	Accommodation accom = accomService.selectAccommodation(review.getAccommId());
	    	accomName = accom.getAccomName();
	    	review.setAccommName(accomName);
	    }
	    
	    model.addAttribute("list", list);
	    
	    return "/mypage/reviewlist";
	}
	
	//리뷰 삭제 ajax
	@RequestMapping(value="/mypage/reviewdelete", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> reviewDelete(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String reviewId = HttpUtil.get(request, "reviewId", "");
		
		try
		{
			Review review = reviewService.reviewSelect(reviewId);
			
			if(reviewService.reviewDelete(reviewId) > 0)
			{
				String accommId = review.getAccommId();
				double count = 0; 
				count = reviewService.reviewRatingAvg(accommId);
				Accommodation accomm = new Accommodation();
				accomm.setAccomAvg(count);
				accomm.setAccomId(accommId);
				accomService.accommRateAverage(accomm);
				ajaxResponse.setResponse(0, "review delete success" + count);
			}
			else
			{
				ajaxResponse.setResponse(-1, "review delete fail");
			}
		}
		catch(Exception e)
		{
			ajaxResponse.setResponse(500, "internal server error");
		}
		
		return ajaxResponse;
	}
}
