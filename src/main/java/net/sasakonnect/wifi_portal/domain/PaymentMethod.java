package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethod extends BasePortalDomain {
    @Column()
    String name;
    
    
    @Column()
    Boolean isActive;
    
    @Column()
    String description;
    
    @Column()
    String url;
    
}
