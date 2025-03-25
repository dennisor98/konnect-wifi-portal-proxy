package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.NotificationReqDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.annotations.PaymentSdkFilter;
import net.sasakonnect.wifi_portal.services.NotificationService;

@CustomController
@RequestMapping("notification")
@Tag(name="Notification")
public class NotificationsController {
	@Autowired
	NotificationService notificationService;

	@PostMapping("/send")
//	@PaymentSdkFilter()
	public Object sendMessage(
	@Valid @RequestBody() NotificationReqDto req,
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
		return this.notificationService.createNotification(req);

	}
	
	@GetMapping("/user")
	public Object getUserNotificatons(
			@RequestParam(name="pageNumber",defaultValue="0") Integer pageNumber,
			@RequestParam(name="pageSize",defaultValue="10") Integer pageSize
			
			) {
		if(pageSize > 10) {
			pageSize = 10;
		}
		
		PageRequest pageable = PageRequest.of(pageNumber, pageSize,Sort.Direction.DESC,"createdAt");
		return this.notificationService.getUserMessages(pageable);
	}
	
	@PutMapping("/read")
	public Object setRead(@RequestParam(name="messageId",required=true) String messageId) {
		return this.notificationService.setRead(messageId);
	}
}
