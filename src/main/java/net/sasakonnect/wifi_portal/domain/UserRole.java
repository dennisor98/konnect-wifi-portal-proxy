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
public class UserRole extends BasePortalDomain {
  @OneToOne()
  @JoinColumn(name="user_id",nullable=false)
  User user;
  
  @ManyToOne()
  @JoinColumn(name="role_id",nullable=false,unique=false)
  Role role;
  
  
  @ManyToOne()
  @JoinColumn(name="creator",nullable=true,unique=false)
  User creator;
  
  
}
