package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangeDeviceDto {
	@NotNull(message = "oldMac is required")
	@Pattern(regexp = ".{12,}", message = "oldMac must be at least 12 characters long")
	String oldMac;
  
  @NotNull(message="newMac is required")
  @Pattern(regexp = ".{12,}", message = "newMac must be at least 12 characters long")
  String newMac;
  
  
}
