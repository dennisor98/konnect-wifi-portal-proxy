package net.sasakonnect.wifi_portal.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.PollTxStatusDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.ToolkitPayDto;
import net.sasakonnect.wifi_portal.RequestDto.sdk.PaymentRequest;
import net.sasakonnect.wifi_portal.annotations.PaymentSdkFilter;
import net.sasakonnect.wifi_portal.annotations.RateLimit;
import net.sasakonnect.wifi_portal.beans.AppRequestBean;
import net.sasakonnect.wifi_portal.beans.PackagePricesBean;
import net.sasakonnect.wifi_portal.services.PaymentService;
import net.sasakonnect.wifi_portal.services.RabbitMqSenderService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
@RequestMapping("sdk")
@Tag(name="Sdk")
@RestController
@Slf4j
public class PaymentSdkController {
	
	@Autowired
	private AppRequestBean requestScopedBean;
	@Autowired
	PaymentService paymentService;

	@Autowired
	PackagePricesBean packageBean;

	@Autowired 
	RabbitMqSenderService rabitMqSenderService;
	@PostMapping("mpesa/stkPush")
	@ApiOperation(value = "Initialize M-Pesa STK Push", notes = "This endpoint initializes the M-Pesa STK push request")
	@PaymentSdkFilter
	public Object mpesaStkPushInit(
			@RequestHeader(value = "App-Key") 
			@Parameter(description = "The App Key used for authentication", 
			required = true, 
			example = "f7bc83f430538424b13298e6aa6fb143efd8427454f7f9a3e49e91d90c416b0e") 
			String appKey,

			@RequestHeader(value = "App-Secret") 
			@Parameter(description = "The App Secret used for authentication", 
			required = true, 
			example = "1d8d6cf2c65c0f2875e6b79f675bd1e5ad3b90f9b4e18f649134d8f5c8f94e7d") 
			String appSecret,

			@Valid @RequestBody 
			@Parameter(description = "STK Push DTO containing request details", 
			required = true) 
			PaymentRequest stk) {

		var currentApp= this.requestScopedBean.getApp();
		stk.setAppKey(currentApp.getAppKey());
		stk.setApp(currentApp);
		return  this.rabitMqSenderService.sendPaymentRequest(stk);

		// return this.paymentService.triggerMpesaStkPush(stk,currentApp);

	}


	@ApiOperation(value = "Request for direct till payment", notes = "This endpoint creates and queues for validation of direct cash payment to till")
	@PaymentSdkFilter
	@PostMapping("mpesa/toolkitPay")
	public Object requestToolkitPayment(@Valid @RequestBody ToolkitPayDto payReq,
			@RequestHeader(value = "App-Key") 
	@Parameter(description = "The App Key used for authentication", 
	required = true, 
	example = "f7bc83f430538424b13298e6aa6fb143efd8427454f7f9a3e49e91d90c416b0e") 
	String appKey,

	@RequestHeader(value = "App-Secret") 
	@Parameter(description = "The App Secret used for authentication", 
	required = true, 
	example = "1d8d6cf2c65c0f2875e6b79f675bd1e5ad3b90f9b4e18f649134d8f5c8f94e7d") 
	String appSecret) {
		log.error("{payload}"+payReq);
		return this.paymentService.createMerchantPaymentRequest(payReq);
	}
	
	


	@ApiOperation(value = "Get Transaction status By Id", notes = "This endpoint gets transaction status by Id")
//	 @PaymentSdkFilter
	 @RateLimit(maxRequests = 1000, durationSeconds = 10)
	@PostMapping("transaction/status/query")
	public Object getTxStatusById(@Valid @RequestBody PollTxStatusDto req,
 			@RequestHeader(value = "App-Key") 
			@Parameter(description = "The App Key used for authentication", 
			required = true, 
			example = "f7bc83f430538424b13298e6aa6fb143efd8427454f7f9a3e49e91d90c416b0e") 
			String appKey,

			@RequestHeader(value = "App-Secret") 
			@Parameter(description = "The App Secret used for authentication", 
			required = true, 
			example = "1d8d6cf2c65c0f2875e6b79f675bd1e5ad3b90f9b4e18f649134d8f5c8f94e7d") 
			String appSecret
			) {
		log.error("transQuery"+req);

		return this.paymentService.getPaymentStatusByTxId(req);
	}
	
	
	@PostMapping("/transaction/txStatus")
	public Object queryTx(
			@RequestParam(name="txId") String txId
			) {
		
		try {
			return this.paymentService.getTransactionStatus(txId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}


}
