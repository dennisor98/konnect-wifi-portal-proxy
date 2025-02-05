package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePackageDto {
	@NotNull
	String id;
	@NotNull
	String name;
	@NotNull
	Integer cost;
	@NotNull
	Integer noOfUsers;
	@NotNull
	String description;
	@NotNull
	String promotionText;
	@NotNull
	Boolean onPromotion;
	@NotNull
	Boolean active;
	
}
