package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.Notification;
import net.sasakonnect.wifi_portal.domain.NotificationsRead;
import net.sasakonnect.wifi_portal.domain.User;

public interface NotificationsReadRepository extends JpaRepository<NotificationsRead,String>{
   Optional<NotificationsRead> findByUserAndMessage(User user,Notification notification);
}
