package net.sasakonnect.wifi_portal.domain;

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
@Entity
public class UserReferal extends BasePortalDomain{
    @ManyToOne()
    @JoinColumn(name="referal_code_id")
    ReferralCode referer;
    
    
    @OneToOne()
    @JoinColumn(name="user_id")
    User user;
}