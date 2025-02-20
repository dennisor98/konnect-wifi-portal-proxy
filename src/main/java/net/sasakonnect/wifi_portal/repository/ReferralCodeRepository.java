package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.ReferralCode;
import net.sasakonnect.wifi_portal.domain.User;

public interface ReferralCodeRepository extends JpaRepository<ReferralCode,String> {
  Optional<ReferralCode> findByUser(User user);
  Optional<ReferralCode> findByCode(String code);
}
