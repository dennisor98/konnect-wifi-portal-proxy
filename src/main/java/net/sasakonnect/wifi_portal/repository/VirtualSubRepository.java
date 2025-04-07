package net.sasakonnect.wifi_portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.VirtualSub;

public interface VirtualSubRepository extends JpaRepository<VirtualSub,String>{
    
}
