package net.sasakonnect.wifi_portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserDevices;

public interface UserDevicesRepository extends JpaRepository<UserDevices,String> {
   Optional<UserDevices> findByUserAndStaMac(User user,String staMac);
   
   List<UserDevices> findByUser(User user);
}
