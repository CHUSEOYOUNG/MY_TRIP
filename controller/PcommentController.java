package com.sist.web.controller;

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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.sist.common.util.StringUtil;
import com.sist.web.model.Editor;
import com.sist.web.model.Paging;
import com.sist.web.model.Pcomment;
import com.sist.web.model.Response;
import com.sist.web.service.PcommentService;
import com.sist.web.util.CookieUtil;
import com.sist.web.util.HttpUtil;

@Controller("pcommentController")
public class PcommentController {

	private static Logger logger = LoggerFactory.getLogger(PcommentController.class);
	
	@Autowired
	private PcommentService pcommentService;
	
	//댓글 등록
	@RequestMapping(value="/editor/commentInsert", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> commentInsert(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String planId = HttpUtil.get(request, "planId", "");
		String planCommentContent = HttpUtil.get(request, "planCommentContent", "");
		String userId = (String) request.getSession().getAttribute("userId");
		if(!StringUtil.isEmpty(planCommentContent))
		{
			Pcomment pcomment = new Pcomment();
			
			pcomment.setPlanCommentContent(planCommentContent);
			pcomment.setPlanId(planId);
			pcomment.setUserId(userId);
			
			try
			{
				if(pcommentService.pcommentInsert(pcomment) > 0)
				{
					ajaxResponse.setResponse(0, "success");
				}
				else
				{
					ajaxResponse.setResponse(500, "internal server error");					
				}
			}
			catch(Exception e)
			{
				logger.error("[PcommentController]commentInsert Exception", e);
				ajaxResponse.setResponse(500, "internal server error2");
			}
			
		}
		else
		{
			ajaxResponse.setResponse(400, "bad request");
		}
		
		return ajaxResponse;
	}
	
	//댓글 삭제
	@RequestMapping(value="/editor/commentDelete", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> commentDelete(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		int commentId = HttpUtil.get(request, "commentId", 0);
		
		if(commentId > 0)
		{
			try
			{
				if(pcommentService.pcommentDelete(commentId) > 0)
				{
					ajaxResponse.setResponse(0, "success");
				}
				else
				{
					ajaxResponse.setResponse(500, "server error1");
				}
			}
			catch(Exception e)
			{
				logger.error("[HiBoardController]delete Exception", e);
				ajaxResponse.setResponse(500, "server error2");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "bad request");
		}
		
		return ajaxResponse;
	}
}
