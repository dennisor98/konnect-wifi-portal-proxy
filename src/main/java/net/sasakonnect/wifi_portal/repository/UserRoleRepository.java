package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole,String>{
  Optional<UserRole> findByUserAndRole(User user,Role role);
  @Query("SELECT ur.role FROM UserRole ur WHERE ur.user =:user")
  Optional<Role> findRoleByUser(@Param("user") User user);
}
