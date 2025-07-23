package com.sist.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sist.common.util.StringUtil;
import com.sist.web.model.Editor;
import com.sist.web.model.OrderDetail;
import com.sist.web.model.Region;
import com.sist.web.model.Response;
import com.sist.web.model.Seller;
import com.sist.web.service.OrderService;
import com.sist.web.service.SellerService;
import com.sist.web.util.CookieUtil;
import com.sist.web.util.HttpUtil;
import com.sist.web.util.JsonUtil;

@Controller("sellerController")
public class SellerController 
{
	private static Logger logger = LoggerFactory.getLogger(SellerController.class);
	
	@Autowired
	private SellerService sellerService;
	@Autowired
	private OrderService orderService;
	
	@Value("#{env['auth.seller.name']}")
	private String AUTH_SELLER_NAME; 
	
	// /seller 했을 때 셀러 메인으로 가게
	@RequestMapping(value = "/seller", method=RequestMethod.GET)
	public String seller(HttpServletRequest request, HttpServletResponse response, Model model)
	{
		return "/seller/sellerMain";
	}
	
	@RequestMapping(value = "/seller/sellerMain", method=RequestMethod.GET)
	public String sellerMain(HttpServletRequest request, HttpServletResponse response, Model model)
	{

		return "/seller/sellerMain";
	}
	
	//판매자 로그인페이지
	@RequestMapping(value = "/seller/login", method=RequestMethod.GET)
	public String login(HttpServletRequest request, HttpServletResponse response)
	{
		return "/seller/login";
	}
	
	
	//로그인
	@RequestMapping(value="/seller/loginProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> loginProc(HttpServletRequest request, 
			HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String sellerId = HttpUtil.get(request, "sellerId");
		String sellerPassword = HttpUtil.get(request, "sellerPassword");
		
		if(!StringUtil.isEmpty(sellerId) && !StringUtil.isEmpty(sellerPassword))
		{
			Seller seller = sellerService.sellerSelect(sellerId);
			
			if(seller != null)
			{
				if(StringUtil.equals(sellerPassword, seller.getSellerPassword()))
				{
					CookieUtil.addCookie(response, "/", -1, AUTH_SELLER_NAME, 
                            CookieUtil.stringToHex(sellerId));
			
					// 로그인 성공 → 세션 저장
			        HttpSession session = request.getSession(true); // 세션이 없으면 생성
			        session.setAttribute("sellerId", sellerId);
			        
			    	logger.debug("userId : " + sellerId);
					logger.debug("userPassword hex : " + CookieUtil.stringToHex(sellerId));
					ajaxResponse.setResponse(0, "success");
				}
				else
				{
					//비밀번호 불일치
					ajaxResponse.setResponse(-1, "password mismatch");
					
				}
			}
			else
			{
				ajaxResponse.setResponse(404, "not found");
			}

		}
		else
		{
			ajaxResponse.setResponse(400, "Bad Request");
		}
		if(logger.isDebugEnabled())
		{
			logger.debug("[SellerController]/seller/login response \n" +
										JsonUtil.toJsonPretty(ajaxResponse));
		}
		
		return ajaxResponse;
	}
	
	
	//판매자 로그아웃
	@RequestMapping(value="/seller/loginOut", method=RequestMethod.GET)
	public String loginOut(HttpServletRequest request, HttpServletResponse response)
	{
		
		// 세션 종료
	    request.getSession().invalidate(); 
	    HttpSession session = request.getSession(false); // 세션이 있으면 가져오고, 없으면 null
	    if (session != null) {
	        session.invalidate(); // 세션종료
	    }
	    
		if(CookieUtil.getCookie(request, AUTH_SELLER_NAME) != null)
		{
			CookieUtil.deleteCookie(request, response, "/", AUTH_SELLER_NAME);
		}
		
		return "redirect:/seller/login";
	}
	
	//판매자 가입화면
	@RequestMapping(value="/seller/sellerRegForm", method=RequestMethod.GET)
	public String sellerRegForm(HttpServletRequest request, HttpServletResponse response)
	{
		
		return "/seller/sellerRegForm";
	}
	
	//판매자 아이디 중복체크
	@RequestMapping(value="/seller/idCheck", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> idCheck(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String sellerId = HttpUtil.get(request, "sellerId");
		
		if(!StringUtil.isEmpty(sellerId))
		{
			if(sellerService.sellerSelect(sellerId)== null)
			{
				//사용가능한 아이디
				ajaxResponse.setResponse(0, "success");
			}
			else
			{
				//중복아이디 발생
				ajaxResponse.setResponse(100, "deplicate id");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "bad request");
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("[SellerController]/seller/idCheck response \n" + 
									JsonUtil.toJsonPretty(ajaxResponse));
		}
		return ajaxResponse;
	}
	
	//판매자등록
	@RequestMapping(value="/seller/sellerRegProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> sellerRegProc(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		String sellerId = HttpUtil.get(request, "sellerId", "");
		String sellerPassword = HttpUtil.get(request, "sellerPassword2");
		String sellerName = HttpUtil.get(request, "sellerName", "");
		String sellerNumber = HttpUtil.get(request, "sellerNumber", "");
		String sellerEmail = HttpUtil.get(request, "sellerEmail", "");
		String sellerBusiness = HttpUtil.get(request, "sellerBusiness","");
		String sellerSellnumber = HttpUtil.get(request, "sellerSellnumber","");
		
		if(!StringUtil.isEmpty(sellerId) && !StringUtil.isEmpty(sellerPassword) && !StringUtil.isEmpty(sellerName) &&
				!StringUtil.isEmpty(sellerNumber) && !StringUtil.isEmpty(sellerEmail) && 
				 	!StringUtil.isEmpty(sellerBusiness) && !StringUtil.isEmpty(sellerSellnumber))
		{
			if(sellerService.sellerSelect(sellerId)==null)
			{
				Seller seller = new Seller();
				seller.setSellerId(sellerId);
				seller.setSellerPassword(sellerPassword);
				seller.setSellerName(sellerName);
				seller.setSellerNumber(sellerNumber);
				seller.setSellerEmail(sellerEmail);
				seller.setSellerBusiness(sellerBusiness);
				seller.setSellerSellnumber(sellerSellnumber);
				
				if(sellerService.sellerInsert(seller) > 0)
				{
					ajaxResponse.setResponse(0, "success");
				}
				else
				{
					ajaxResponse.setResponse(500, "internal server error");
				}
			}
			else
			{
				//중복 아이디 존재시
				ajaxResponse.setResponse(100, "duplicate id");
			}
		}
		else
		{
			ajaxResponse.setResponse(400, "bad request");
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("[SellerController]/seller/sellerRegProc response \n" +
										JsonUtil.toJsonPretty(ajaxResponse));
		}
		return ajaxResponse;
	}
	
	//판매자정보 수정화면
	@RequestMapping(value="/seller/sellerUpdateForm", method=RequestMethod.GET)
	public String sellerUpdateForm(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		//쿠키를 가져옴
		String cookieSellerId = CookieUtil.getHexValue(request, AUTH_SELLER_NAME);
		
		Seller seller = sellerService.sellerSelect(cookieSellerId);
		
		model.addAttribute("seller", seller);
		
		return "/seller/sellerUpdateForm";
	}
	
	//판매자정보 수정
	@RequestMapping(value="/seller/sellerUpdateProc", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> sellerUpdateProc(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		
		String sellerId = HttpUtil.get(request, "sellerId", "");
		String sellerName = HttpUtil.get(request, "sellerName", "");
		String sellerNumber = HttpUtil.get(request, "sellerNumber", "");
		String sellerEmail = HttpUtil.get(request, "sellerEmail", "");
		String sellerBusiness = HttpUtil.get(request, "sellerBusiness","");
		String sellerSellnumber = HttpUtil.get(request, "sellerSellnumber","");
		
		
		String cookieSellerId = CookieUtil.getHexValue(request, AUTH_SELLER_NAME);
		
		if(!StringUtil.isEmpty(cookieSellerId))
		{
			if(StringUtil.equals(sellerId, cookieSellerId))
			{
				Seller seller = sellerService.sellerSelect(cookieSellerId);
				
				if(seller != null)
				{
					if(!StringUtil.isEmpty(sellerId) && !StringUtil.isEmpty(sellerName) &&
							!StringUtil.isEmpty(sellerNumber) && !StringUtil.isEmpty(sellerEmail) && 
							 	!StringUtil.isEmpty(sellerBusiness) && !StringUtil.isEmpty(sellerSellnumber))
					{
						seller.setSellerId(sellerId);
						seller.setSellerNumber(sellerNumber);
						seller.setSellerBusiness(sellerBusiness);
						seller.setSellerEmail(sellerEmail);
						seller.setSellerSellnumber(sellerSellnumber);
						
						if(sellerService.sellerUpdate(seller) > 0)
						{
							ajaxResponse.setResponse(0, "success");
						}
						else
						{
							ajaxResponse.setResponse(500, "internal server error");
						}
						
					}
					else
					{
						//파라미터 값이 올바르지 않을 경우
						ajaxResponse.setResponse(400, "bad request");
					}	
					
				}
				else
				{
					//사용자 정보가 없는경우
					CookieUtil.deleteCookie(request, response, "/", AUTH_SELLER_NAME);
					ajaxResponse.setResponse(404, "not found");
				}
			}
			else
			{
				//쿠키정보와 넘어온 userId가 다른 경우
				CookieUtil.deleteCookie(request, response, "/", AUTH_SELLER_NAME);
				ajaxResponse.setResponse(430, "id infomation is different");
			}
		}
		else
		{
			ajaxResponse.setResponse(410, "loging failed");
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("[SellerController]/seller/sellerUpdateProc response \n" +
					JsonUtil.toJsonPretty(ajaxResponse));
		}
		

		return ajaxResponse;
	}
	
	//판매자 비밀번호변경화면
	@RequestMapping(value="/seller/sellerPasswordForm", method=RequestMethod.GET)
	public String sellerPasswordForm(HttpServletRequest request, HttpServletResponse response)
	{
		return "/seller/sellerPasswordForm";
	}
	
	//판매자 패스워드변경
	@RequestMapping(value="/seller/sellerPasswordChange", method=RequestMethod.POST)
	@ResponseBody
	public Response<Object> sellerPasswordChange(HttpServletRequest request, HttpServletResponse response)
	{
		Response<Object> ajaxResponse = new Response<Object>();
		String cookieSellerId = CookieUtil.getHexValue(request, AUTH_SELLER_NAME);
		
		String newPassword = HttpUtil.get(request, "sellerPassword");
		
		Seller seller = sellerService.sellerSelect(cookieSellerId);
		seller.setSellerPassword(newPassword);
		
		if(sellerService.sellerPasswordChange(seller) > 0)
		{
			ajaxResponse.setResponse(0, "success");
		}
		else
		{
			ajaxResponse.setResponse(500, "internal server error");
		}
		
		if(logger.isDebugEnabled())
		{
			logger.debug("[UserController]/seller/sellerPasswordChange response \n" +
					JsonUtil.toJsonPretty(ajaxResponse));
		}
		
		return ajaxResponse;
	}
	
	//현재비밀번호 체크
	@RequestMapping(value="/seller/checkCurrentPassword")
	@ResponseBody
	public boolean checkCurrentPassword(HttpServletRequest request)
	{
		String currentPassword = HttpUtil.get(request, "currentPassword");
		String cookieSellerId = CookieUtil.getHexValue(request, AUTH_SELLER_NAME);
		logger.debug("입력한 비밀번호:>>>>>>>>>>><<<<<<<<<<<<<<<<"+currentPassword);
		
		Seller seller = sellerService.sellerSelect(cookieSellerId);
		logger.debug("DB 비밀번호: >>>>>>>>>>><<<<<<<<<<<<<<<<" + seller.getSellerPassword());
		return seller.getSellerPassword().equals(currentPassword);
	
	}
	
	@RequestMapping(value="/seller/roomPriceUpdateForm")
	public String roomPriceUpdateForm(@RequestParam("accomId") String accomId, Model model) {
		
		return "/seller/roomPriceUpdateForm";
	}

	// 판매자 페이지 정산 내역 띄워주는 거
	@RequestMapping(value="/seller/sellerSellList", method=RequestMethod.GET)
	public String sellerSellList(ModelMap model, HttpServletRequest request, HttpServletResponse response)
	{
		String cookieSellerId = CookieUtil.getHexValue(request, AUTH_SELLER_NAME);
		
		List<OrderDetail> list = new ArrayList<>();
		int sum = 0;
		list = orderService.sellerSellList(cookieSellerId);
		sum = orderService.sellerSum(cookieSellerId);
		
		model.addAttribute("list", list);
		model.addAttribute("sum", sum);
    	
		return "/seller/sellerSellList";
	}
}
