package net.sasakonnect.wifi_portal.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sasakonnect.wifi_portal.RequestDto.SaveDeviceDto;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserDevices;
import net.sasakonnect.wifi_portal.repository.UserDevicesRepository;

@Service
public class UserDeviceService {
	
	@Autowired
	UserDevicesRepository devicesRepository;
	 public Object saveUserDevice(SaveDeviceDto save) {
		 User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		 Optional<UserDevices> userDeviceopt = this.devicesRepository.findByUserAndStaMac(user, save.getStaMac());
		 if(userDeviceopt.isPresent()) {
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", false);
			 res.put("message", "Devices already saved");
			 
			 return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
		 }
		 try {
			 UserDevices device = UserDevices.builder().name(save.getName()).staMac(save.getStaMac()).user(user).build();
			 this.devicesRepository.save(device);
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", true);
			 res.put("message", "Device saved successfuly");
			 return ResponseEntity.status(HttpStatus.OK).body(res);
		 }catch(Exception ex) {
			ex.printStackTrace();
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", false);
			 res.put("message", "A server error was encountered");
			 
			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		 }
		 
	 }
	 
	 public Object editUserDevice(SaveDeviceDto deviceDto) {
		 User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		 Optional<UserDevices> userDeviceopt = this.devicesRepository.findByUserAndStaMac(user, deviceDto.getStaMac());
		 
		 if(userDeviceopt.isEmpty()) {
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", false);
			 res.put("message", "Device not found");
			 
			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		 }
		 UserDevices device =  userDeviceopt.get();
		 device.setName(deviceDto.getName());
		 device.setStaMac(deviceDto.getStaMac());
		 
		 try {
			 this.devicesRepository.save(device);
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", true);
			 res.put("message", "Device edited");
			 
			 return ResponseEntity.status(HttpStatus.OK).body(res);
		 }catch(Exception ex) {
			 ex.printStackTrace();
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", false);
			 res.put("message", "Something went wrong");
			 
			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		 }
		 
	 }
	 
	 public Object deleteUserDevice(String staMac) {
		 User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		 Optional<UserDevices> userDeviceopt = this.devicesRepository.findByUserAndStaMac(user,staMac);
		 if(userDeviceopt.isEmpty()) {
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", false);
			 res.put("message", "Device not found"); 

			 return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		 }

		 var device = userDeviceopt.get();
		 try {
			 this.devicesRepository.delete(device);
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", true);
			 res.put("message", "Device removed");

			 return ResponseEntity.status(HttpStatus.OK).body(res);
		 }catch(Exception ex) {
			 ex.printStackTrace();
			 ObjectNode res =  JsonNodeFactory.instance.objectNode();
			 res.put("success", false);
			 res.put("message", "Something went wrong");

			 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		 }
		 
	 }
	 
	 public Object getUserDevices() {
		 User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		 List<UserDevices> devicesList = this.devicesRepository.findByUser(user);
		 var devices = devicesList.stream()
				 .map(d->{
					 ObjectNode dev = JsonNodeFactory.instance.objectNode();
					 dev.put("id",d.getId());
					 dev.put("staMac",d.getStaMac());
					 dev.put("name",d.getName());
					 dev.put("createdAt",d.getCreatedAt().toString());
					 dev.put("updatedAt",d.getUpdatedAt().toString());
                 return dev;

				 }).collect(Collectors.toList());
		 Map<String,Object> res = new HashMap<>();
		 res.put("success",true);
		 res.put("devices",devices);
		 return ResponseEntity.status(HttpStatus.OK).body(res);
	 }
}
