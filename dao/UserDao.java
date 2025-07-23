package com.sist.web.dao;

import org.springframework.stereotype.Repository;

import com.sist.web.model.User;

@Repository("userDao")
public interface UserDao 
{	//@Repository : 해당 클래스에 발생하는 DB관련 예외를 DAOException으로 전환.
	
//	//회원탈퇴
//	public int userWithdrawal(String userId);	
	
//	///////////////////////////////
	//회원조회
	public User userSelect(String userId);
	
	//회원등록 
	public int userInsert(User user);
	
	//회원정보 수정
	public int userUpdate(User user);
	
	//회원 비밀번호변경
	public int userPasswordChange(User user);
	
	//회원탈퇴
	public int userWithdrawal(String userId);
	//고유ID조회
	public User selectUniqueId(String uniqueId);

}
