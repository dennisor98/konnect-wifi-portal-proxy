package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
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
public class ReferralCode extends BasePortalDomain{
  @Column(nullable=false,unique=true)
  String code;
  
  @OneToOne()
  @JoinColumn(name="user_id")
  User user;
  
  @Column()
  Boolean isActive;
}
