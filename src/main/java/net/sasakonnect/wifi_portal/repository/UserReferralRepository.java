package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserReferal;

public interface UserReferralRepository extends JpaRepository<UserReferal,String>{
	Optional<UserReferal> findByUser(User user);
	
	@Query("SELECT ur FROM UserReferal WHERE ur.referer.user.id =: refId")
	Page<UserReferal> findByReferer(@Param("refId") String refId,Pageable pageable);

}
