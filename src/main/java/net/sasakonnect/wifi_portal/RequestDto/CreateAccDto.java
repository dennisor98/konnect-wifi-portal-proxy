package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAccDto {
   @NotNull(message="phone is required")
   String phone;
   
   @NotNull(message="firstname is required")
   String firstname;
   
   @NotNull(message="lastname is required")
   String lastname;
   
   String email;
   
   String champCode;
   
   
   
   
}
