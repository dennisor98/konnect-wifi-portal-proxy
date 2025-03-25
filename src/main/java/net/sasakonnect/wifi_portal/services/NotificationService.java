package net.sasakonnect.wifi_portal.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sasakonnect.wifi_portal.BasePagingUtility;
import net.sasakonnect.wifi_portal.RequestDto.NotificationReqDto;
import net.sasakonnect.wifi_portal.domain.Notification;
import net.sasakonnect.wifi_portal.domain.NotificationsRead;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.repository.NotificationRepository;
import net.sasakonnect.wifi_portal.repository.NotificationsReadRepository;

@Service
public class NotificationService extends BasePagingUtility<Notification> {
	@Autowired
	NotificationRepository notificationRepository;
	@Autowired
	NotificationsReadRepository notificationReadRepository;
	@Autowired
	FirebaseService firebaseService;
     public Object createNotification(NotificationReqDto notifictaionReq) {
    	 ObjectNode res = JsonNodeFactory.instance.objectNode();
    	 if(notifictaionReq.getIsPublic()) {
    		 this.firebaseService.sendGeneralNotification(notifictaionReq);
    		 return 0;
    	 }
    	 if(notifictaionReq.getReceiverId().isEmpty()) {
    		 
    		 res.put("success", false);
    		 res.put("message","private messages must have recipient contacts");
    		 
    		 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    	 }
		 this.firebaseService.sendMessage(notifictaionReq);

    	 //send batch messages to individual topics
    	 
    	 return null;
     }
     
     public Object getUserMessages(Pageable pageable) {
    	 try {
    		 User user  = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    		 Page<Notification> notifsPage = this.notificationRepository.findByIsPublicTrueOrReceiver(user, pageable);
    		 var notifs  = notifsPage.stream()
    				 .map((n)->{
    					 Map<String,Object> map = new HashMap<>();
    					 map.put("id",n.getId());
    					 map.put("title",n.getTitle());
    					 map.put("message",n.getMessage());
    					 map.put("caption",n.getCaption());
    					 map.put("isPublic",n.getIsPublic());
    					 map.put("isRead",n.getIsPublic() ? (this.notificationReadRepository.findByUserAndMessage(user, n).isPresent() ? true : false) : n.getIsRead() );
    					 map.put("createdAt",n.getCreatedAt());
    					 map.put("updatedAt",n.getUpdatedAt());
    					 return map;
    				 }).collect(Collectors.toList());
    		 Map<String,Object> res =  new HashMap<>();
    		 res.put("success",true);
    		 res.put("message","Request completed");
    		 
    		 Map<String,Object> payload =  new HashMap<>();
    		 payload.put("notifications",notifs);
    		 payload.put("pagination",this.getPaginationInfo(notifsPage));
    		 
    		 res.put("payload", payload);
    		 
    		 return ResponseEntity.status(HttpStatus.OK).body(res);
    		 
    	 }catch(Exception ex) {
    		 ex.printStackTrace();
    		 Map<String,Object> res =  new HashMap<>();
    		 res.put("success",false);
    		 res.put("message","Error ocurred while processing request");
    		 
    		 
    		 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    		 
    	 }
    	
    	 
     }
     
     
     public Object setRead(String messageId) {
    	 User user  = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 		Optional<Notification> notifOpt =  this.notificationRepository.findById(messageId);
        if(notifOpt.isEmpty()) {
        	Map<String,Object> res = new HashMap<>();
        	res.put("success", false);
        	res.put("message","Invalid messageId");
        	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
        }
        Notification notif = notifOpt.get();
        if(!notif.getIsPublic()) {
        	notif.setIsRead(true);
        	try {
        		this.notificationRepository.save(notif);
        		Map<String,Object> res = new HashMap<>();
        		res.put("success", true);
        		res.put("message","Message Read");
        		return ResponseEntity.status(HttpStatus.OK).body(res);
        	}catch(Exception ex) {
        		ex.printStackTrace();
        		Map<String,Object> res = new HashMap<>();
        		res.put("success", false);
        		res.put("message","Error while processing request");
        		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);	
        	}
        	
        }
        
        try {
        	//create user read record if it does not exist
        	Optional<NotificationsRead> notifReadOpt = this.notificationReadRepository.findByUserAndMessage(user, notif);
        	if(notifReadOpt.isEmpty()) {
                NotificationsRead notifReadBuild =  NotificationsRead.builder().message(notif).user(user).build();
                this.notificationReadRepository.save(notifReadBuild);
                
                Map<String,Object> res = new HashMap<>();
            	res.put("success",true);
            	res.put("message","Message Read");
            	return ResponseEntity.status(HttpStatus.OK).body(res);
        	}
        }catch(Exception ex) {
        	Map<String,Object> res = new HashMap<>();
        	res.put("success", false);
        	res.put("message","Error while processing request");
        	return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
        }
    	 return null;
     }
     
     
     
     public Object getUserNotifications() {
    	 return null;
     }
}
