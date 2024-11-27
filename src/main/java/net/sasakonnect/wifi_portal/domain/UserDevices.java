package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity()
public class UserDevices extends BasePortalDomain {
    @ManyToOne()
    @JoinColumn(name="user_id")
    User user;
    
    @Column(nullable=false)
    String staMac;
    
    @Column()
    String name;
}
