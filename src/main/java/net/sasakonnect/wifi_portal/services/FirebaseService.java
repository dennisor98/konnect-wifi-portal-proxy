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
           if (notification.getReceiverId().size() > 0) {
               notification.getReceiverId().stream()
                   .forEach(topic -> conditionBuilder.append("'").append(topic).append("' in topics || "));
               
               if (conditionBuilder.length() > 0) {
                   conditionBuilder.setLength(conditionBuilder.length() - 4);
               }
           }
           String condition = conditionBuilder.toString();
          var ntf = Notification.builder().setBody("Hello").setTitle("Greetings").build();
		 Message message = Message.builder()
		     .putData("title",notification.getTitle())
		     .putData("message",notification.getMessage())
		     .putData("caption",notification.getCaption())
		     .setNotification(ntf)
		     .setCondition(condition)
		     .build();

		 String response;
//		 log.error("{message}"+messag);
		try {
			response = FirebaseMessaging.getInstance().send(message);
			 System.out.println("Successfully sent message to firebase: " + response);
		} catch (FirebaseMessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
}
