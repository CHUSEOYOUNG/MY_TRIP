package com.sist.web.dao;

import com.sist.web.model.Report;

public interface ReportDao {

	//신고 했나 조회
	public int reportInquiry(Report report);
	//신고 insert
	public int reportInsert(Report report);
}
