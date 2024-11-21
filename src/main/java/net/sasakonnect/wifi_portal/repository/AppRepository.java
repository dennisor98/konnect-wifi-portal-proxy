package net.sasakonnect.wifi_portal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import net.sasakonnect.wifi_portal.domain.App;
import net.sasakonnect.wifi_portal.domain.Payment;

public interface AppRepository extends JpaRepository<App, String> {
	@Query(value = "SELECT * FROM app a WHERE a.app_key = :appKey AND a.app_secret = :appSecret LIMIT 1", nativeQuery = true)
	Optional<App> findFirstByAppKeyAndAppSecret(@Param("appKey") String appKey, @Param("appSecret") String appSecret);
	
	
	@Query(value = "SELECT * FROM app a WHERE a.app_key = :appKey  LIMIT 1", nativeQuery = true)
	Optional<App> findFirstByAppKeyAndAppSecret(@Param("appKey") String appKey);

	
	
	
}
