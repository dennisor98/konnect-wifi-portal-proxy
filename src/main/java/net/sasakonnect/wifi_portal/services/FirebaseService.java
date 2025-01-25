package net.sasakonnect.wifi_portal.services;


import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.NotificationReqDto;


@Service
@Slf4j
public class FirebaseService {
	public void sendMessage(NotificationReqDto notification) {
	    StringBuilder conditionBuilder = new StringBuilder();

	    // Build the condition string
	    if (notification.getReceiverId() != null && !notification.getReceiverId().isEmpty()) {
	        notification.getReceiverId().forEach(topic -> 
	            conditionBuilder.append("'").append(topic).append("' in topics || ")
	        );

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
	        log.info("Successfully sent message to Firebase: {}", response);
	    } catch (FirebaseMessagingException e) {
	        log.error("Error sending message to Firebase: {}", e.getMessage(), e);
	    }
	}

}
