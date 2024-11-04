package net.sasakonnect.wifi_portal.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class InternetPackages  extends BasePortalDomain{
   @Column()
   String foreignPackageId;
   
   @Column()
   String name;
   
   @Column()
   Integer cost;
   
   @Column()
   Date foreignCreatedAt;
   
   @Column()
   Boolean active;
   
   @Column()
   Integer noOfUsers;
   
   @Column()
   String zone;
   
   @Column()
   Boolean isNew;
   
   
   
}
