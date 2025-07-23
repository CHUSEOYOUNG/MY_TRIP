package com.sist.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.RefundDao;
import com.sist.web.model.Refund;

@Service("RefundService")
public class RefundService {
	
	@Autowired
	private RefundDao refundDao;
	

	public void inserRefund(Refund refund) {
		refund.setRefundId(refundDao.getRefundSeq());
		refundDao.insertRefund(refund);
	}
	
	public List<Refund> getRefundByOrderId(String orderId){
		return refundDao.getRefundByOrderId(orderId);
	}
	
	public void updateRefundResult(String refundId, String result) {
		Map<String, Object> param = new HashMap<>();
		param.put("refundId", refundId);
		param.put("refundResult",result);
		refundDao.updateRefundResult(param);
	}
	
}
