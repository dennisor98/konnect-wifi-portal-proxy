package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.sasakonnect.wifi_portal.domain.User;

public interface UserRepository extends JpaRepository<User,String>{
  Optional<User> findByPhone(String phone);
  Optional<User> findById(String id);
  Optional<User> findByUserId(String userId);
  @Query("SELECT u FROM User u WHERE u.phone LIKE %:phone%")
  Page<User> searchByPhone(@Param("phone") String phone,Pageable pageable);
}
