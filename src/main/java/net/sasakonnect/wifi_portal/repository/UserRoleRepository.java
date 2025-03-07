package net.sasakonnect.wifi_portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole,String>{
  Optional<UserRole> findByUserAndRole(User user,Role role);
  Optional<UserRole> findByUser(User user);
  @Query("SELECT ur.role FROM UserRole ur WHERE ur.user =:user")
  Optional<Role> findRoleByUser(@Param("user") User user);
  @Query("SELECT ur.user FROM UserRole ur WHERE ur.role =:role")
  List<User> findRoleUsers(@Param("role") Role role);
}
