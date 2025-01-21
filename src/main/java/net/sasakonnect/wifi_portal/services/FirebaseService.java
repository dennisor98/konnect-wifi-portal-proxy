package net.sasakonnect.wifi_portal.services;


import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import net.sasakonnect.wifi_portal.RequestDto.NotificationReqDto;


@Service
public class FirebaseService {
	public void sendMessage(NotificationReqDto notification) {
		   StringBuilder conditionBuilder = new StringBuilder();
           if (notification.getReceiverId().size() > 0) {
               notification.getReceiverId().stream()
                   .forEach(topic -> conditionBuilder.append("'").append(topic).append("' in topics || "));
               
               // Remove the trailing " || "
               if (conditionBuilder.length() > 0) {
                   conditionBuilder.setLength(conditionBuilder.length() - 4);
               }
           }
           String condition = conditionBuilder.toString();
		 Message message = Message.builder()
		     .putData("title",notification.getTitle())
		     .putData("message",notification.getMessage())
		     .putData("caption",notification.getCaption())
		     .setCondition(condition)
		     .build();

		 String response;
		try {
			response = FirebaseMessaging.getInstance().send(message);
			 System.out.println("Successfully sent message to firebase: " + response);
		} catch (FirebaseMessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
}
