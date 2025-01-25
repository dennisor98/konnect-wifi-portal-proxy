package net.sasakonnect.wifi_portal.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sasakonnect.wifi_portal.RequestDto.NotificationReqDto;
import net.sasakonnect.wifi_portal.domain.Notification;
import net.sasakonnect.wifi_portal.repository.NotificationRepository;

@Service
public class NotificationService {
	@Autowired
	NotificationRepository notificationRepository;
	@Autowired
	FirebaseService firebaseService;
     public Object createNotification(NotificationReqDto notifictaionReq) {
    	 ObjectNode res = JsonNodeFactory.instance.objectNode();
//    	 if(notifictaionReq.getIsPublic()) {
//    		 this.firebaseService.sendMessage(notifictaionReq);
//    		 //send to public firebase topic
//    		var ntf =  Notification.builder().isPublic(notifictaionReq.getIsPublic()).message(notifictaionReq.getMessage()).messageType(notifictaionReq.getMessageType())
//    				   .receiver(null).title(notifictaionReq.getTitle()).build();
//    		this.notificationRepository.save(ntf);
//    		
//    	 }
    	 if(notifictaionReq.getReceiverId().isEmpty()) {
    		 
    		 res.put("success", false);
    		 res.put("message","private messages must have recipient contacts");
    		 
    		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    	 }
		 this.firebaseService.sendMessage(notifictaionReq);

    	 //send batch messages to individual topics
    	 
    	 return null;
     }
     
     public Object createBatchNotifications() {
    	 return null;
     }
}
