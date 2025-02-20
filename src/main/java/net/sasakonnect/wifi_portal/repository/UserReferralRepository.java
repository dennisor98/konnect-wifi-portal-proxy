package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserReferal;

public interface UserReferralRepository extends JpaRepository<UserReferal,String>{
	Optional<UserReferal> findByUser(User user);

}
