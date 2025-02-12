package net.sasakonnect.wifi_portal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.sasakonnect.wifi_portal.domain.Permission;
import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.domain.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission,String> {
   Optional<RolePermission> findByRoleAndPermission(Role role ,Permission permission);
   List<RolePermission> findByRole(Role role);
   
   
   @Query("SELECT  p FROM RolePermission rp JOIN  rp.permission p WHERE rp.role =:role")
   List<Permission> findPermissionsByRole(@Param("role") Role role);
   
}
