package net.sasakonnect.wifi_portal.domain;

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
public class Role extends BasePortalDomain {
   
	@Column()
	String name;
	
	@Column()
	String description;
}
