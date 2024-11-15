package net.sasakonnect.wifi_portal.RequestDto;

import lombok.Data;

@Data
public class DataDto {
	String name;
	String publicIp;
	String model;
	VlanDto vlan;
	   
}
