package net.sasakonnect.wifi_portal.ResponseDto;

import java.util.List;

import org.springframework.http.HttpStatus;

import lombok.Data;

@Data
public class PackageResponseDto {
	HttpStatus status;
	String success;

	List<InternetPackageDto> payload;
}
