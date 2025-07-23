package com.sist.web.controller;

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

import com.sist.web.model.Report;
import com.sist.web.model.Response;
import com.sist.web.service.EditorService;
import com.sist.web.service.ReportService;
import com.sist.web.util.HttpUtil;

@Controller("reportController")
public class ReportController {
	private static Logger logger = LoggerFactory.getLogger(ReportController.class);
	
	@Autowired
	private ReportService reportService;
	@Autowired
	private EditorService editorService;
	
	@RequestMapping(value = "/editor/reportPopup", method=RequestMethod.GET)
	public String reportPopup(HttpServletRequest request, HttpServletResponse response, ModelMap model)
	{
		String planId = request.getParameter("planId");
	    model.addAttribute("planId", planId);
		
	    return "/editor/reportPopup";
	}
	
	//신고
	@RequestMapping(value="/editor/report", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> report(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String planId = HttpUtil.get(request, "planId", "");
		String userId = HttpUtil.get(request, "userId", "");
		
		int reason = HttpUtil.get(request, "reason", 0);

		if (!planId.equals(""))
		{
			try
			{
				Report report = new Report();
				
				report.setPlanId(planId);
				report.setUserId(userId);
				report.setReportReason(reason);

				if (reportService.reportInquiry(report) > 0)
				{
					ajaxResponse.setResponse(-1, "already report");
				}
				else
				{
					if (reportService.reportInsert(report) > 0)
					{
						if (editorService.editorReport(Integer.parseInt(planId)) > 0)
						{
							ajaxResponse.setResponse(0, "report success");
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.error("[ReportController]report Exception", e);
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
