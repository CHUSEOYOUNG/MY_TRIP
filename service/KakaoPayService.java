package com.sist.web.service;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.sist.common.util.StringUtil;
import com.sist.web.model.KakaoPayApproveRequest;
import com.sist.web.model.KakaoPayApproveResponse;
import com.sist.web.model.KakaoPayReadyRequest;
import com.sist.web.model.KakaoPayReadyResponse;

@Service("kakaoPayService")
public class KakaoPayService {
	private static Logger logger = LoggerFactory.getLogger(KakaoPayService.class);
	
	/*
		각각 카카오페이 api 따올때 받은 id, 시크릿키, 성공/실패시 url 등
	 */
	@Value("#{env['kakaopay.client.id']}")
	private String KAKAOPAY_CLIENT_ID;

	@Value("#{env['kakaopay.client.secret']}")
	private String KAKAOPAY_CLIENT_SECRET;

	@Value("#{env['kakaopay.secret.key']}")
	private String KAKAOPAY_SECRET_KEY;

	@Value("#{env['kakaopay.ready.url']}")
	private String KAKAOPAY_READY_URL;

	@Value("#{env['kakaopay.approval.url']}")
	private String KAKAOPAY_APPROVAL_URL;

	@Value("#{env['kakaopay.client.success.url']}")
	private String KAKAOPAY_CLIENT_SUCCESS_URL;

	@Value("#{env['kakaopay.client.cancel.url']}")
	private String KAKAOPAY_CLIENT_CANCEL_URL;

	@Value("#{env['kakaopay.client.fail.url']}")
	private String KAKAOPAY_CLIENT_FAIL_URL;

	private HttpHeaders kakaoPayHeaders;
	// 카카오페이 Request 헤더설정
	@PostConstruct
	private void postConstruct()
	{
		kakaoPayHeaders = new HttpHeaders();
		
		kakaoPayHeaders.set("Authorization", "SECRET_KEY " + KAKAOPAY_SECRET_KEY);	
		kakaoPayHeaders.set("Content-Type", "application/json"); 
	}
	
	// 카카오페이 Ready 호출(결제 준비)
	public KakaoPayReadyResponse ready(KakaoPayReadyRequest kakaoPayReadyRequest)
	{
		KakaoPayReadyResponse kakaoPayReadyResponse = null;
		
		StringBuilder log = new StringBuilder();
		
		log.append("\n======================================================");
		log.append("\n#[KakaoPayService]ready");
		log.append("\n======================================================");
		
		if(kakaoPayReadyRequest !=null)
		{
			log.append(kakaoPayReadyRequest.toString());
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("cid", KAKAOPAY_CLIENT_ID); // 가맹점코드 10자리
			
			if(!StringUtil.isEmpty(kakaoPayReadyRequest.getCid_secret()))
			{
				parameters.put("cid_secret",  kakaoPayReadyRequest.getCid_secret());
			}
			
			parameters.put("partner_order_id",kakaoPayReadyRequest.getPartner_order_id());
			parameters.put("partner_user_id", kakaoPayReadyRequest.getPartner_user_id());
			parameters.put("item_name", kakaoPayReadyRequest.getItem_name() );
			
			if(!StringUtil.isEmpty(kakaoPayReadyRequest.getItem_code()))
			{
				parameters.put("item_code", kakaoPayReadyRequest.getItem_code());
			}
			parameters.put("quantity", kakaoPayReadyRequest.getQuantity()); // 상품 수량
			parameters.put("total_amount", kakaoPayReadyRequest.getTotal_amount()); // 상품 총액
			parameters.put("tax_free_amount", kakaoPayReadyRequest.getTax_free_amount()); // 비과세 금액
			
			
			// int 들은 0보다 큰가 ?
			if(kakaoPayReadyRequest.getVat_amount() > 0) {
				parameters.put("vat_amount", kakaoPayReadyRequest.getVat_amount()); // 부가세 (선택)
			}
		    

			if(kakaoPayReadyRequest.getGreen_deposit() >0) {
				parameters.put("green_deposit", kakaoPayReadyRequest.getGreen_deposit()); // 컵 보증금 (선택)
			}
		    


			parameters.put("approval_url", KAKAOPAY_CLIENT_SUCCESS_URL); // 결제 성공 시 redirect
			parameters.put("cancel_url", KAKAOPAY_CLIENT_CANCEL_URL);     // 결제 취소 시 redirect
			parameters.put("fail_url", KAKAOPAY_CLIENT_FAIL_URL);         // 결제 실패 시 redirect

			if (!StringUtil.isEmpty(kakaoPayReadyRequest.getAvailable_cards())) {
			    parameters.put("available_cards", kakaoPayReadyRequest.getAvailable_cards()); // 카드사 제한 (선택)
			}

			if (!StringUtil.isEmpty(kakaoPayReadyRequest.getPayment_method_type())) {
			    parameters.put("payment_method_type", kakaoPayReadyRequest.getPayment_method_type()); // 카드 / 머니 선택
			}
			
			if( kakaoPayReadyRequest.getInstall_month() > 0)
			{
				parameters.put("install_month", kakaoPayReadyRequest.getInstall_month()); // 할부 개월 수
			}
			


			if (!StringUtil.isEmpty(kakaoPayReadyRequest.getUse_share_installment())) {
			    parameters.put("use_share_installment", kakaoPayReadyRequest.getUse_share_installment()); // 분담무이자 여부
			}

			if (!StringUtil.isEmpty(kakaoPayReadyRequest.getCustom_json())) {
			    parameters.put("custom_json", kakaoPayReadyRequest.getCustom_json()); // 사용자 정의 JSON
			}
			
			logger.debug("11111111111111111111111111111111111111");
			
			// Http Header와 Http Body 설정
			// 요청하기 ㅜㅇ해 header와 Body를 합치기
			// 스프링에서 제공해주는 HttpEntity 클래스에 header와 body를 합칩ㄴ디ㅏ ..?
			
			HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<Map<String, Object>>(parameters, kakaoPayHeaders);
			
			// Spring에서 제공하는 http통신 처리를 위한 유틸리티 클래스
			// 클라이언트(이 경우에는 카카오페이가 서버임) 쪽에서 http 요청을 만들고, 서버의 응답을 처리하는데 유용함
			RestTemplate restTemplate = new RestTemplate();
			// postForEntity : Post 요청 보내고, 응답은 ResponseEntity로 받는다.
			ResponseEntity<KakaoPayReadyResponse> responseEntity = restTemplate.postForEntity(KAKAOPAY_READY_URL, requestEntity, KakaoPayReadyResponse.class);
			logger.debug("22222222222222222222222222222222222");
			
			if(responseEntity != null)
			{
				log.append("\nready statusCode : " + responseEntity.getStatusCode());
				
				kakaoPayReadyResponse = responseEntity.getBody();
				
				if(kakaoPayReadyResponse != null)
				{
					log.append("\nready body : \n" + kakaoPayReadyResponse);
				}
				else
				{
					log.append("\nready body : body is null");
				}
			}
			else
			{
				log.append("\nready : ResponseEntity is null");
			}
		}
		log.append("\n======================================================");
		logger.info(log.toString());
		
		return kakaoPayReadyResponse;
	}
	
	//카카오페이 결제 승인 요청
	public KakaoPayApproveResponse approve(KakaoPayApproveRequest kakaoPayApproveRequest)
	{
		KakaoPayApproveResponse kakaoPayApproveResponse = null;
		
		StringBuilder log = new StringBuilder();
		
		log.append("\n==================================================");
		log.append("\n==[KakaoPayService] approve");
		log.append("\n==================================================");
		
		if(kakaoPayApproveRequest !=null)
		{
			log.append(kakaoPayApproveRequest.toString());
			
			Map<String, Object> parameters = new HashMap<String, Object>();
			
			parameters.put("cid", KAKAOPAY_CLIENT_ID);
			if(!StringUtil.isEmpty(kakaoPayApproveRequest.getCid_secret())){
				parameters.put("cid_secret",KAKAOPAY_CLIENT_SECRET);
			}

			parameters.put("tid", kakaoPayApproveRequest.getTid());  // 결제 고유번호 (ready 응답에 포함)
			parameters.put("partner_order_id", kakaoPayApproveRequest.getPartner_order_id());  // 가맹점 주문번호
			parameters.put("partner_user_id", kakaoPayApproveRequest.getPartner_user_id());  // 가맹점 회원 ID
			parameters.put("pg_token", kakaoPayApproveRequest.getPg_token());  // 결제 승인 토큰

			if (!StringUtil.isEmpty(kakaoPayApproveRequest.getPayload())) {
			    parameters.put("payload", kakaoPayApproveRequest.getPayload());
			}

			if (kakaoPayApproveRequest.getTotal_amount() > 0) {
			    parameters.put("total_amount", kakaoPayApproveRequest.getTotal_amount());
			}
			
			HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<Map<String, Object>>(parameters, kakaoPayHeaders);
			RestTemplate restTemplate = new RestTemplate();
			
			ResponseEntity<KakaoPayApproveResponse> responseEntity = restTemplate.postForEntity(KAKAOPAY_APPROVAL_URL, requestEntity, KakaoPayApproveResponse.class);
			
			if(responseEntity != null)
			{
				kakaoPayApproveResponse = responseEntity.getBody();
				
				if(kakaoPayApproveResponse !=null)
				{
					log.append("\napprove body : \n" + kakaoPayApproveResponse);
				}
				else {
					log.append("\napprove body : body is null");
				}
			}
			else
			{
				log.append("\napprove : ResponseEntity is null");
				
			}
		}
		
		logger.info(log.toString());
		
		return kakaoPayApproveResponse;
	}
	
	// 카카오페이 결제 취소 (환불 요청)
	public boolean cancel(String tid, int cancelAmount) {
	    StringBuilder log = new StringBuilder();

	    log.append("\n==================================================");
	    log.append("\n==[KakaoPayService] cancel");
	    log.append("\n==================================================");

	    boolean result = false;

	    if (!StringUtil.isEmpty(tid) && cancelAmount > 0) {
	        log.append("\n[tid] " + tid);
	        log.append("\n[cancelAmount] " + cancelAmount);

	        Map<String, Object> parameters = new HashMap<>();
	        parameters.put("cid", KAKAOPAY_CLIENT_ID);
	        parameters.put("tid", tid);
	        parameters.put("cancel_amount", cancelAmount);
	        parameters.put("cancel_tax_free_amount", 0); // 비과세 금액 없을 경우 0

	        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(parameters, kakaoPayHeaders);
	        RestTemplate restTemplate = new RestTemplate();

	        try {
	            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
	                "https://open-api.kakaopay.com/online/v1/payment/cancel", // 최신 cancel URL
	                requestEntity,
	                String.class
	            );

	            log.append("\ncancel statusCode : " + responseEntity.getStatusCode());
	            log.append("\ncancel body : \n" + responseEntity.getBody());

	            result = responseEntity.getStatusCode().is2xxSuccessful();
	        } catch (Exception e) {
	            log.append("\ncancel exception: " + e.getMessage());
	            logger.error("카카오페이 환불 중 예외 발생", e);
	        }
	    } else {
	        log.append("\ncancel 요청값 누락됨 (tid 또는 cancelAmount)");
	    }

	    log.append("\n==================================================");
	    logger.info(log.toString());

	    return result;
	}

}
