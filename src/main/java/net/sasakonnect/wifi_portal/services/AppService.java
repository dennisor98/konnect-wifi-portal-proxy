package net.sasakonnect.wifi_portal.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sasakonnect.wifi_portal.RequestDto.CreateAppDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdateAppDto;
import net.sasakonnect.wifi_portal.domain.App;
import net.sasakonnect.wifi_portal.repository.AppRepository;
@Service
public class AppService {
	@Autowired
	AppRepository appRepository;
	
	public Object createApp(CreateAppDto payload) {
		Optional<App> appNameOpt =  this.appRepository.findByName(payload.getAppName());
		if(appNameOpt.isPresent()) {
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			
			res.put("success",false);
			res.put("message","App with this name already exists");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		
		Optional<App> appOpt = this.appRepository.findFirstByAppKeyAndAppSecret(payload.getAppKey(),payload.getAppSecret());
		if(appOpt.isPresent()) {
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","App already exists");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		
		var appBuild =  App.builder().appKey(payload.getAppKey()).appSecret(payload.getAppSecret())
				        .businessShortCode(payload.getBusinessShortCode()).callbackUrl(payload.getCallBackUrl())
				        .consumerKey(payload.getConsumerKey()).consumerSecret(payload.getConsumerSecret())
				        .mpesaTillNo(payload.getMpesaTillNo()).isActive(payload.getIsActive())
				        .name(payload.getAppName())
				        .build();
		try {
			this.appRepository.save(appBuild);
			ObjectNode res = JsonNodeFactory.instance.objectNode();

			res.put("success",true);
			res.put("message","App created");
			
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			ObjectNode res = JsonNodeFactory.instance.objectNode();

			res.put("success",false);
			res.put("message","Server error encountered");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
				
	}
	
	public Optional<App> findAppByAppKeyAndSecret(String appKey,String secret) {
		return this.appRepository.findFirstByAppKeyAndAppSecret(appKey, secret);
	}
	public Optional<App> findAppByAppKey(String appKey) {
		return this.appRepository.findFirstByAppKeyAndAppSecret(appKey);
	}
	
	
	public Optional<App> findAppByName(String appName){
		return this.appRepository.findByName(appName);
	}
	
	public Object getApps() {
		List<App> appList = this.appRepository.findAll();
		var apps = appList.stream()
				.map(ap->{
					Map<String,Object> map = new HashMap<>();
					map.put("id", ap.getId());
					map.put("name", ap.getName());
					map.put("appKey", ap.getAppKey());
					map.put("appSecret", ap.getAppSecret());
					map.put("businessShortCode", ap.getBusinessShortCode());
					map.put("tillNo", ap.getMpesaTillNo());
					map.put("callBackUrl", ap.getCallbackUrl());
					map.put("active", ap.getIsActive());
					return map;
				}).collect(Collectors.toList());
		Map<String,Object> res = new HashMap<>();
		Map<String,Object> payload = new HashMap<>();
		payload.put("success",true);
		payload.put("message","Request completed");
		payload.put("apps",apps);
		res.put("payload",payload);
		return ResponseEntity.status(HttpStatus.OK).body(res);
	}
	
	public Object updateApp(UpdateAppDto payload) {
		Optional<App> appOpt = this.appRepository.findById(payload.getAppId());
		if(appOpt.isEmpty()) {
			Map<String,Object> res = new HashMap<>();
			res.put("success",false);
			res.put("message","Invalid appId");	
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		var app = appOpt.get();
		app.setAppKey(payload.getAppKey());
		app.setAppSecret(payload.getAppSecret());
		app.setBusinessShortCode(payload.getBusinessShortCode());
		app.setCallbackUrl(payload.getCallBackUrl());
		app.setConsumerKey(payload.getConsumerKey());
		app.setConsumerSecret(payload.getConsumerSecret());
		app.setIsActive(payload.getIsActive());
		app.setMpesaTillNo(payload.getMpesaTillNo());
		app.setName(payload.getAppName());
		try {
			this.appRepository.save(app);
			Map<String,Object> res = new HashMap<>();
			res.put("success",true);
			res.put("message","App updated");	
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			Map<String,Object> res = new HashMap<>();
			res.put("success",false);
			res.put("message","A server error encountered");	
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
	}
	
	public Object deleteApp(String appId) {
		Optional<App> appOpt = this.appRepository.findById(appId);
		if(appOpt.isEmpty()) {
			Map<String,Object> res = new HashMap<>();
			res.put("success",false);
			res.put("message","Invalid appId");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		
		var app = appOpt.get();
		try {
			this.appRepository.delete(app);
			Map<String,Object> res = new HashMap<>();
			res.put("success",true);
			res.put("message","App deleted");
			
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			Map<String,Object> res = new HashMap<>();
			res.put("success",false);
			res.put("message","A server error encountered");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
	}

}
