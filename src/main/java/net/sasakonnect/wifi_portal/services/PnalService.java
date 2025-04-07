package net.sasakonnect.wifi_portal.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import net.sasakonnect.wifi_portal.RequestDto.BulkVirtualSubDto;
import net.sasakonnect.wifi_portal.RequestDto.VirtualSubDto;
import net.sasakonnect.wifi_portal.beans.ThreadExecuterBean;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.VirtualSub;
import net.sasakonnect.wifi_portal.repository.VirtualSubRepository;

@Service
public class PnalService {
	@Autowired
	UserService userService;
	@Autowired
	PaymentService paymentService;
	@Autowired
	VirtualSubRepository vsubRepository;
	@Autowired
	ThreadExecuterBean threadExecBean;
	public Object createVirtualSubScription(VirtualSubDto vsub) {
		Optional<User> userOpt = userService.findUserByPhone(vsub.getMobileNumber());
		if(userOpt.isEmpty()) {
			Map<String,Object> res =  new HashMap<>();
			res.put("success",false);
			res.put("message","User account unavailable");

			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
		}

		var vsubBuild =  VirtualSub.builder()
				.amount(vsub.getAmount())
				.isActive(true).name("GIFT")
				.packageId(this.paymentService.getPackageIdByCost(vsub.getAmount()))
				.user(userOpt.get())
				.build();

		try {
			this.vsubRepository.save(vsubBuild);
			Map<String,Object> res = new HashMap<>();
			res.put("success","Activate later package created");

			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			ex.printStackTrace();
			Map<String,Object> res = new HashMap<>();
			res.put("success",false);
			res.put("message","Server error while processing request");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
	}
	
	public Object createBulkVirtualSubs(List<VirtualSubDto> vsubs) {
		if(vsubs.isEmpty()) {
			Map<String,Object> res =  new HashMap<>();
			res.put("success",false);
			res.put("message","List cannot be empty");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		
		List<VirtualSub> vsubList =  new ArrayList<>();
		List<Map<String,Object>> failed =  new ArrayList<>();
		this.threadExecBean.addTask(new  Runnable(){

			@Override
			public void run() {
				vsubs.stream()
				.map((s)->{
					String mobile = s.getMobileNumber().trim();
					if(mobile.length() < 9) {
						Map<String,Object> error =  new HashMap<>();
						error.put("contact",mobile);
						error.put("reason","Invalid phone number");
						failed.add(error);
					}
					Optional<User> userOpt =  userService.findUserByPhone(mobile.substring(mobile.length() -9));
					if(userOpt.isEmpty()) {
						Map<String,Object> error =  new HashMap<>();
						error.put("contact","+254"+mobile);
						error.put("reason","Account not found");
						failed.add(error);
					}
					
					var vsubBuild =  VirtualSub.builder()
							.amount(s.getAmount())
							.isActive(true)
							.name("GIFT")
							.packageId(paymentService.getPackageIdByCost(s.getAmount()))
							.user(userOpt.get())
							.build();
					
					vsubList.add(vsubBuild);
					
					return vsubList;
				}).collect(Collectors.toList());
				
				
			}
			
		});
		try {
			vsubRepository.saveAll(vsubList);
			Map<String,Object> res = new HashMap<>();
			res.put("success",true);
			res.put("message","Subscriptions generated");
			res.put("failed", failed);
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			ex.printStackTrace();
			Map<String,Object> res = new HashMap<>();
			res.put("success",false);
			res.put("message","Server error creating subscriptions");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
	}
	
	
	public Object createBulkVirtualSubsV2(BulkVirtualSubDto vsubs) {
		if(vsubs.getContacts().isEmpty()) {
			Map<String,Object> res =  new HashMap<>();
			res.put("success",false);
			res.put("message","Contacts cannot be empty");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		
		List<VirtualSub> vsubList =  new ArrayList<>();
		List<Map<String,Object>> failed =  new ArrayList<>();
		this.threadExecBean.addTask(new  Runnable(){

			@Override
			public void run() {
				vsubs.getContacts().stream()
				.map((c)->{
					String mobile = c.trim();
					if(mobile.length() < 9) {
						Map<String,Object> error =  new HashMap<>();
						error.put("contact",mobile);
						error.put("reason","Invalid phone number");
						failed.add(error);
					}
					Optional<User> userOpt =  userService.findUserByPhone(mobile.substring(mobile.length() -9));
					if(userOpt.isEmpty()) {
						Map<String,Object> error =  new HashMap<>();
						error.put("contact","+254"+mobile);
						error.put("reason","Account not found");
						failed.add(error);
					}
					
					var vsubBuild =  VirtualSub.builder()
							.amount(vsubs.getAmount())
							.isActive(true)
							.name("GIFT")
							.packageId(paymentService.getPackageIdByCost(vsubs.getAmount()))
							.user(userOpt.get())
							.build();
					
					vsubList.add(vsubBuild);
					
					return vsubList;
				}).collect(Collectors.toList());
				
				
			}
			
		});
		try {
			vsubRepository.saveAll(vsubList);
			Map<String,Object> res = new HashMap<>();
			res.put("success",true);
			res.put("message","Subscriptions generated");
			res.put("failed", failed);
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			ex.printStackTrace();
			Map<String,Object> res = new HashMap<>();
			res.put("success",false);
			res.put("message","Server error creating subscriptions");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
	}
}
