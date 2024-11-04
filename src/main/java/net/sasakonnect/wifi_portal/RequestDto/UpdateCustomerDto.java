package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCustomerDto {
	@NotNull
   String firstName;
   
   @NotNull
   String lastName;
}
