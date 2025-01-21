package net.sasakonnect.wifi_portal.RequestDto;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationReqDto {
   List<String> receiverId;
   
   @NotNull
   String message;
   
   @NotNull
   String title;
   
   String caption;
   
   @NotNull
   Boolean isPublic;
   
   @NotNull
   String messageType;
}
