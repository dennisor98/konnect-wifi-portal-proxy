package net.sasakonnect.wifi_portal.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sasakonnect.wifi_portal.domain.App;
import net.sasakonnect.wifi_portal.repository.AppRepository;
@Service
public class AppService {
	@Autowired
	AppRepository appRepository;
	public Optional<App> findAppByAppKeyAndSecret(String appKey,String secret) {
		return this.appRepository.findFirstByAppKeyAndAppSecret(appKey, secret);
	}
	public Optional<App> findAppByAppKey(String appKey) {
		return this.appRepository.findFirstByAppKeyAndAppSecret(appKey);
	}
	
	
	public Optional<App> findAppByName(String appName){
		return this.appRepository.findByName(appName);
	}
	
}
