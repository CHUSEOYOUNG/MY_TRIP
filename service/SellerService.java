package com.sist.web.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.SellerDao;
import com.sist.web.model.Seller;

@Service("sellerService")
public class SellerService {
	private static Logger logger = LoggerFactory.getLogger(SellerService.class);
	
	@Autowired
	private SellerDao sellerDao;
	
	
	//판매자조회
	public Seller sellerSelect(String sellerId)
	{
		Seller seller = null;
		try
		{
			seller = sellerDao.sellerSelect(sellerId);
		}
		catch(Exception e)
		{
			logger.error("[SellerService]sellerSelect Exception", e);
		}
		return seller;
		
	}
	
	//판매자정보 등록
	public int sellerInsert(Seller seller)
	{
		int count = 0;
		
		try
		{
			count = sellerDao.sellerInsert(seller);
		}
		catch(Exception e)
		{
			logger.error("[SellerService]sellerInsert Exception", e);
		}
		return count;
	}
	
	//판매자정보 수정
	public int sellerUpdate(Seller seller)
	{
		int count = 0;
		
		try
		{
			count = sellerDao.sellerUpdate(seller);
		}
		catch(Exception e)
		{
			logger.error("[SellerService]sellerUpdate Exception", e);
		}
		return count;
	}
	
	//판매자비밀번호 변경
	public int sellerPasswordChange(Seller seller)
	{
		int count = 0;
		
		try
		{
			count = sellerDao.sellerPasswordChange(seller);
		}
		catch(Exception e)
		{
			logger.error("[SellerService]sellerPasswordChange Exception", e);
		}
		return count;
	}
	
	
	
	
}
