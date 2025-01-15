package net.sasakonnect.wifi_portal.RequestDto;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationReqDto {
   List<String> phoneNumber;
   
   @NotNull
   String message;
   
   @NotNull
   String title;
   
   @NotNull
   Boolean isPublic;
   
   @NotNull
   String messageType;
}
