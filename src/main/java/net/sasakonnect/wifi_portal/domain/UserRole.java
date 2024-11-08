package net.sasakonnect.wifi_portal.domain;

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
public class UserRole extends BasePortalDomain {
  @OneToOne()
  @JoinColumn(name="user_id",nullable=false)
  User user;
  
  @OneToOne()
  @JoinColumn(name="role_id",nullable=false)
  Role role;
  
  @OneToOne()
  @JoinColumn(name="creator",nullable=true)
  User creator;
  
  
}
