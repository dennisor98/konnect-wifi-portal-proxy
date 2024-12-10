package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.InternetPackages;

public interface InternetPackageRepository extends JpaRepository<InternetPackages,String> {
   Optional<InternetPackages> findByForeignPackageId(String id);
   Optional<InternetPackages> findByCost(String cost);
}
