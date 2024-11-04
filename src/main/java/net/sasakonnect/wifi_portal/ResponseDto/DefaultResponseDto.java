package net.sasakonnect.wifi_portal.ResponseDto;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class DefaultResponseDto {
	HttpStatus status;
	String success;
}
