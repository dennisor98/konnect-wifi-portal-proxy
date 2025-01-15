package net.sasakonnect.wifi_portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.Notification;

public interface NotificationRepository extends JpaRepository<Notification,String> {
  
}
