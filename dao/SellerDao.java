package com.sist.web.dao;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.sist.web.model.Seller;

@Repository("sellerDao")
public interface SellerDao 
{
	//판매자 조회
	public Seller sellerSelect(String sellerId);
	
	//판매자 정보등록
	public int sellerInsert(Seller seller);
	
	//판매자 정보수정
	public int sellerUpdate(Seller seller);
	
	//판매자 비밀번호변경
	public int sellerPasswordChange(Seller seller);
	
	

}
