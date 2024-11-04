package net.sasakonnect.wifi_portal.ResponseDto;

import lombok.Data;

@Data
public class InternetPackageDto {
  String createdAt;
  
  String updated_at;
  
  String deletedAt;
  
  String id;
  
  String name;
  
  String description;
  
  Integer cost;
  
  Boolean active;
  
  Integer noOfUsers;
  
  String zone;
  
}
