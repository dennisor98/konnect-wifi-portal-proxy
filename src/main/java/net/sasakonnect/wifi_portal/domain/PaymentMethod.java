package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity()
public class PaymentMethod extends BasePortalDomain {
    @Column()
    String name;
    
    @Column()
    Boolean isActive;
    
    @Column()
    String description;
    
    @Column()
    String url;
    
    @ManyToOne()
    @JoinColumn(name="creator_user_id",nullable=false)
    User user;
    
    
}
