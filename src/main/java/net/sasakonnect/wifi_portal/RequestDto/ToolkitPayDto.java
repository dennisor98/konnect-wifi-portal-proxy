package net.sasakonnect.wifi_portal.RequestDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import net.sasakonnect.wifi_portal.serd.AuthAttemptDeserializer;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolkitPayDto {
   @NotNull()
   String mobileNumber;
   
   String appKey;
   
   String userId;
   
   String firstname;
   
   Integer amount;
   @JsonDeserialize(using = AuthAttemptDeserializer.class)
   AuthAttemptDto authAttempt;  
//   @JsonDeserialize(using = AuthAttemptDeserializer.class)
//   AuthAttemptDto authAttemptObject;
   
   String packageId;
   
   String staMac;
   
}


