package net.sasakonnect.wifi_portal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.Notification;
import net.sasakonnect.wifi_portal.domain.User;

public interface NotificationRepository extends JpaRepository<Notification,String> {
	Page<Notification> findByIsPublicTrueOrReceiver(User user, Pageable pageable);
}
