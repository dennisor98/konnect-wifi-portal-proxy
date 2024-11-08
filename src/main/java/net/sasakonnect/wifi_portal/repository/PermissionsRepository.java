package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.sasakonnect.wifi_portal.domain.Permission;

public interface PermissionsRepository extends JpaRepository<Permission,String>{
  Optional<Permission> findByName(String name);
}
