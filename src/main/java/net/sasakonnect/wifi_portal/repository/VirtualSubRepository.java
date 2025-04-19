package net.sasakonnect.wifi_portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.VirtualSub;

public interface VirtualSubRepository extends JpaRepository<VirtualSub,String>{
    List<VirtualSub> findByUser(User user);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VirtualSub> findBySubId(String subId);
    Optional<VirtualSub> findFirstByUserAndIsActiveTrue(User user);
    Optional<VirtualSub> findFirstByUserAndAmount(User user,String amount);

}
