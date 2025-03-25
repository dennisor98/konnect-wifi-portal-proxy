package net.sasakonnect.wifi_portal.services;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.NotificationReqDto;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.repository.NotificationRepository;


@Service
@Slf4j
public class FirebaseService {
	@Autowired
	NotificationRepository notificationRepository;
	@Autowired
	UserService userService;
	public void sendMessage(NotificationReqDto notification) {
	    StringBuilder conditionBuilder = new StringBuilder();
	    
	    List<net.sasakonnect.wifi_portal.domain.Notification> notifbuilds = new ArrayList<>();
        
	    // Build the condition string
	    if (!notification.getIsPublic() && notification.getReceiverId() != null && !notification.getReceiverId().isEmpty()) {
	        notification.getReceiverId().forEach(topic -> {
	        	conditionBuilder.append("'").append(topic).append("' in topics || ");
		        this.userService.findUserByPhone(topic);
		        Optional<User> userOpt = this.userService.findUserByPhone("+254"+topic.substring(topic.length() -9));
	        	 var notif = net.sasakonnect.wifi_portal.domain.Notification.builder().isPublic(notification.getIsPublic()).message(notification.getMessage()).messageType(notification.getMessageType())
			 			   .isRead(false).receiver(userOpt.isPresent() ? userOpt.get() :  null).title(notification.getTitle()).build();
	        	 notifbuilds.add(notif);
	        }   
	        );
	        
	        if(notifbuilds.size() > 0) {
	        	this.notificationRepository.saveAll(notifbuilds);
	        }

	        // Remove the last " || " if it exists
	        if (conditionBuilder.length() > 0) {
	            conditionBuilder.setLength(conditionBuilder.length() - 4);
	        }
	        
	    }

	    String condition = conditionBuilder.toString();
	    log.info("Condition: {}", condition);

	    // Build the notification
	    var ntf = Notification.builder()
	            .setBody(notification.getMessage())
	            .setTitle(notification.getTitle())
	            .build();

	    Message message = Message.builder()
	            .putData("title", notification.getTitle())
	            .putData("message", notification.getMessage())
	            .putData("caption", notification.getCaption())
	            .setNotification(ntf)
	            .setCondition(condition)
	            .build();

	    try {
	        String response = FirebaseMessaging.getInstance().send(message);
	        
//	        this.notificationRepository.save(notif);
	        log.info("Successfully sent message to Firebase: {}", response);
	    } catch (FirebaseMessagingException e) {
	        log.error("Error sending message to Firebase: {}", e.getMessage(), e);
	    }
	}
	
	public void sendGeneralNotification(NotificationReqDto notification) {
		 var ntf = Notification.builder()
		            .setBody(notification.getMessage())
		            .setTitle(notification.getTitle())
		            .build();
		 
		 Message message = Message.builder()
		            .putData("title", notification.getTitle())
		            .putData("message", notification.getMessage())
		            .putData("caption", notification.getCaption())
		            .setNotification(ntf)
		            .setTopic("general")
		            .build();
		 try {
			 String response = FirebaseMessaging.getInstance().send(message);
			var notif = net.sasakonnect.wifi_portal.domain.Notification.builder().isPublic(notification.getIsPublic()).message(notification.getMessage()).messageType(notification.getMessageType())
			   .isRead(false).receiver(null).title(notification.getTitle()).build();
			 this.notificationRepository.save(notif);
			 log.info("Successfully sent message to Firebase: {}", response);
		 } catch (FirebaseMessagingException e) {
			 log.error("Error sending message to Firebase: {}", e.getMessage(), e);
		 }
		 
	}
	
	

}
