package com.sist.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sist.web.model.Recommend;
import com.sist.web.model.Response;
import com.sist.web.service.EditorService;
import com.sist.web.service.RecommendService;
import com.sist.web.util.HttpUtil;

@Controller("recommendController")
public class RecommendController {
	private static Logger logger = LoggerFactory.getLogger(RecommendController.class);
	
	@Autowired
	private RecommendService recommendService;
	@Autowired
	private EditorService editorService;
	
	//좋아요 토글
	@RequestMapping(value="/editor/likeToggle", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> likeToggle(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String likePlanId = HttpUtil.get(request, "likePlanId", "");
		String likeUserId = HttpUtil.get(request, "likeLoginId", "");
		String likeLoginId = (String) request.getSession().getAttribute("userId");
		
		if(likePlanId != "")
		{
			Recommend recom = new Recommend();
			recom.setPlanId(likePlanId);
			recom.setUserId(likeLoginId);
			
			try
			{
				if(recommendService.recommendInquiry(recom) > 0)
				{
					
					if(recommendService.recommendDelete(recom) > 0)
					{
						if(editorService.editorLikeDecre(Integer.parseInt(likePlanId)) > 0)
						{
							ajaxResponse.setResponse(-1, "like delete");
						}
					}
				}
				else
				{
					if(recommendService.recommendInsert(recom) > 0)
					{
						if(editorService.editorLikeIncre(Integer.parseInt(likePlanId)) > 0)
						{
							ajaxResponse.setResponse(0, "like insert");
						}
					}
				}
			}
			catch(Exception e)
			{
				logger.error("[RecommendController]likeToggle Exception", e);
				ajaxResponse.setResponse(500, "server error");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "bad request");
		}
		
		return ajaxResponse;
	}
}
