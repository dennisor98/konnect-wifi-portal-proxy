package net.sasakonnect.wifi_portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.VirtualSub;

public interface VirtualSubRepository extends JpaRepository<VirtualSub,String>{
    List<VirtualSub> findByUser(User user);
    Optional<VirtualSub> findBySubId(String subId);
    Optional<VirtualSub> findFirstByUserAndActiveTrue(User user);

}
