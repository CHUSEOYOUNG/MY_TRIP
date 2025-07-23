package com.sist.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.ReportDao;
import com.sist.web.model.Report;

@Service("reportService")
public class ReportService {

	private static Logger logger = LoggerFactory.getLogger(EditorService.class);
	
	@Autowired
	private ReportDao reportDao;
	
	
	//신고 했나 조회
	public int reportInquiry(Report report)
	{
		int count = 0;
		
		 try
		 {
			 count = reportDao.reportInquiry(report);
		 }
		 catch(Exception e)
		 {
			 logger.error("[ReportService]reportInquiry Exception", e);
		 }
		 
		 return count;
	}
	
	//신고 insert
	public int reportInsert(Report report)
	{
		int count = 0;
		
		 try
		 {
			 count = reportDao.reportInsert(report);
		 }
		 catch(Exception e)
		 {
			 logger.error("[ReportService]reportInsert Exception", e);
		 }
		 
		 return count;
	}
}
