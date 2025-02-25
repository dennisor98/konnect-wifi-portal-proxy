package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePayMethodDto {
	@NotNull
	String id;
	
	@NotNull()
	String name;
    
    @NotNull()
    String description;
    
    @NotNull()
    Boolean isActive;
    
    String apiUrl;
  
}
