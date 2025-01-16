package net.sasakonnect.wifi_portal.RequestDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ToolkitPayDto {
   @NotNull()
   String mobileNumber;
   
   String appKey;
   
   String userId;
   
   String firstname;
   
   Integer amount;
   
   String authAttempt;
   
   String packageId;
   
}
