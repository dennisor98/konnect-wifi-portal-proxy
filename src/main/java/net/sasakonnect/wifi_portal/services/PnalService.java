package net.sasakonnect.wifi_portal.services;

import java.security.SecureRandom;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.BulkVirtualSubDto;
import net.sasakonnect.wifi_portal.RequestDto.VirtualSubDto;
import net.sasakonnect.wifi_portal.beans.ThreadExecuterBean;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.VirtualSub;
import net.sasakonnect.wifi_portal.repository.VirtualSubRepository;

@Service
@Slf4j
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
					
					Optional<VirtualSub> vsubOpt =  vsubRepository.findFirstByUserAndActiveTrue(userOpt.get());
					if(vsubOpt.isPresent()) {
						var vsubBuild =  VirtualSub.builder()
								.amount(s.getAmount())
								.isActive(true)
								.name("GIFT")
								.packageId(paymentService.getPackageIdByCost(s.getAmount()))
								.subId(generateUniqueSubId())
								.user(userOpt.get())
								.build();

						vsubList.add(vsubBuild);
					}
					
					
					return vsubList;
				}).collect(Collectors.toList());
				
				
			}
			
		});
		
		if(!vsubList.isEmpty()) {
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
		
		return null;
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
//		this.threadExecBean.addTask(new  Runnable(){

//			@Override
//			public void run() {
				vsubs.getContacts().stream()
				.map((c)->{
					String mobile = c.trim();
					if(mobile.length() < 9) {
						Map<String,Object> error =  new HashMap<>();
						error.put("contact",mobile);
						error.put("reason","Invalid phone number");
						failed.add(error);
					}
					Optional<User> userOpt =  userService.findUserByPhone("+254"+mobile.substring(mobile.length() -9));
					if(userOpt.isEmpty()) {
						Map<String,Object> error =  new HashMap<>();
						error.put("contact","+254"+mobile);
						error.put("reason","Account not found");
						failed.add(error);
						return 1;
					}
					
					var vsubBuild =  VirtualSub.builder()
							.amount(vsubs.getAmount())
							.isActive(true)
							.name("GIFT")
							.packageId(paymentService.getPackageIdByCost(vsubs.getAmount()))
							.subId(generateUniqueSubId())
							.user(userOpt.get())
							.build();
					
					vsubList.add(vsubBuild);
					
					return vsubList;
				}).collect(Collectors.toList());
				
				
//			}
//			
//		});
		if(!vsubList.isEmpty()) {
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
		
		Map<String,Object> res = new HashMap<>();
		res.put("success",false);
		res.put("message","Subscriptions not generated");
		res.put("failed",failed);
		
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
				
	}
	
	public List<Map<String, Object>> getUserVirtualPackages() {
	    User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	    List<VirtualSub> vsubList = this.vsubRepository.findByUser(user);

	    List<Map<String, Object>> resultList = new ArrayList<>();

	    for (var sub : vsubList) {
	       if(sub.getIsActive()) {
	    	   Map<String, Object> map = new HashMap<>();
		        double randomId = (double) (100_000_000 + new Random().nextInt(900_000_000));
		        map.put("id", randomId);
		        map.put("name", sub.getName());
		        map.put("createdAt",String.valueOf( sub.getCreatedAt()));
		        Calendar calendar = Calendar.getInstance();
		        calendar.setTime(sub.getCreatedAt());
		        calendar.add(Calendar.DAY_OF_MONTH, 30);
		        Date newExpiryDate = calendar.getTime();
		        
		        // Convert Date to ISO 8601 format
		        ZonedDateTime zonedDateTime = newExpiryDate.toInstant().atZone(ZoneId.of("UTC"));
		        String formattedExpiryDate = zonedDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	            
		        map.put("expiryTimestamp", formattedExpiryDate);
		        map.put("amount", sub.getAmount());
		        map.put("active", sub.getIsActive() ? 1.0 : 0.0);
		        map.put("uid", sub.getSubId());
		        map.put("devices",""); // or real devices if available
		       

		        resultList.add(map);
	       }
	    }

	    return resultList;
	}
	
	public List<VirtualSub> getUserVirtualSub(){
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		List<VirtualSub> vsubList = this.vsubRepository.findByUser(user);
		return vsubList;
	}
	
	public Optional<VirtualSub> getVsubByID(String subId ){
		return this.vsubRepository.findBySubId(subId);
	}
	
	public String generateGiftTransId() {
		String PREFIX = "GF";
		int RANDOM_PART_LENGTH = 8;
		String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		SecureRandom random = new SecureRandom();

		StringBuilder sb = new StringBuilder(PREFIX);

		for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
			int index = random.nextInt(CHARACTERS.length());
			sb.append(CHARACTERS.charAt(index));
		}

		return sb.toString();
	}

	
	public static String generateUniqueSubId() {
		 Random RANDOM = new Random();
        StringBuilder sb = new StringBuilder(9);

        // First two uppercase letters
        for (int i = 0; i < 2; i++) {
            char letter = (char) ('A' + RANDOM.nextInt(26));
            sb.append(letter);
        }

        // Next seven digits
        for (int i = 0; i < 7; i++) {
            int digit = RANDOM.nextInt(10);
            sb.append(digit);
        }

        return sb.toString();
    }
}
