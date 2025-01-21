package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.NotificationReqDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.NotificationService;

@CustomController
@RequestMapping("notification")
@Tag(name="Notification")
public class NotificationsController {
	@Autowired
	NotificationService notificationService;
	
	@PostMapping("/send")
    public Object sendMessage(@Valid @RequestBody() NotificationReqDto req) {
		return this.notificationService.createNotification(req);
    	
    }
}
