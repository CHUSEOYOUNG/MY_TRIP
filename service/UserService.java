package com.sist.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sist.web.dao.UserDao;
import com.sist.web.model.User;

@Service("userService")
public class UserService {
	private static Logger logger = LoggerFactory.getLogger(UserService.class);
	
	@Autowired
	private UserDao userDao;
		
//	//회원탈퇴
//	public int userWithdrawal(String userId)
//	{
//		int count = 0;
//		
//		try
//		{
//			count = userDao.userWithdrawal(userId);
//		}
//		catch(Exception e)
//		{
//			logger.error("[UserService]userWithdrawal Exception", e);
//		}
//		
//		return count;
//	}
	
	
	//회원조회
	public User userSelect(String userId)
	{
		User user = null;
		try
		{
			user = userDao.userSelect(userId);
		}
		catch(Exception e)
		{
			logger.error("[UserService]userSelect Exception", e);
		}
		return user;
	}
	
	//회원정보 등록
	public int userInsert(User user)
	{
		int count = 0;
		
		try
		{
			count = userDao.userInsert(user);
		}
		catch(Exception e)
		{
			logger.error("[UserService]userInsert Exception", e);
		}
		
		return count;
	}
	
	//회원정보 수정
	public int userUpdate(User user)
	{
		int count = 0;
		
		try
		{
			count = userDao.userUpdate(user);
		}
		catch(Exception e)
		{
			logger.error("[UserService]userUpdate Exception", e);
		}
		
		return count;
	}
	
	//회원비밀번호 변경
	public int userPasswordChange(User user)
	{
		int count = 0;
		
		try
		{
			count = userDao.userPasswordChange(user);
		}
		catch(Exception e)
		{
			logger.error("[UserService]userPasswordChange Exception", e);
		}
		
		return count;
	}
	

	public int userWithdrawal(String userId)
	{
		int count = 0;
		
		try
		{
			count = userDao.userWithdrawal(userId);
		}
		catch(Exception e)
		{
			logger.error("[UserService]userWithdrawal Exception", e);
		}
		
		return count;
	}
	
	//고유ID조회
	public User selectUniqueId(String uniqueId)
	{
		//int count = 0;
		
		User user = null;
		try
		{
			user = userDao.selectUniqueId(uniqueId);
		}
		catch(Exception e)
		{
			logger.error("[UserService]selectUniqueId Exception", e);
		}
		return user;
	}
}










