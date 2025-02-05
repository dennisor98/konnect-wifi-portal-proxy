package net.sasakonnect.wifi_portal.RequestDto.sdk;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserLoginDto {
     @NotNull(message="phone is missing")
     String phone;
     @NotNull(message="password is missing")
     String password;
}
