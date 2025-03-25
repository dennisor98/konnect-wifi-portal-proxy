package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationsRead extends BasePortalDomain{
   @ManyToOne()
   @JoinColumn(name="message_id")
   Notification message;
   
   
   @ManyToOne()
   @JoinColumn(name="user_id")
   User user;
   
}
