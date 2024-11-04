package net.sasakonnect.wifi_portal.ResponseDto;
import lombok.Data;
@Data
public class VerifyOtpResponseDto {
	private String success;
	private Boolean userExists;
    private String error;	
    private String message;
	private UserObjectDTO payload;
}


