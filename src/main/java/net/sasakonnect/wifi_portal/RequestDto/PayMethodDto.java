package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayMethodDto {
    @NotNull()
	String name;
    
    @NotNull()
    String description;
    
    @NotNull()
    Boolean isActive;
    
    @NotNull()
    String apiUrl;
  
  
}
