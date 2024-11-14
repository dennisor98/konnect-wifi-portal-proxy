package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProfileUploadDto {
 @NotNull(message="base64Image is required")
 String base64Image;
}
