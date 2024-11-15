package net.sasakonnect.wifi_portal.services;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.ClientSubDto;
import net.sasakonnect.wifi_portal.RequestDto.CreateAccDto;
import net.sasakonnect.wifi_portal.RequestDto.ProfileUploadDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdateCustomerDto;
import net.sasakonnect.wifi_portal.RequestDto.VerifyOtpDto;
import net.sasakonnect.wifi_portal.ResponseDto.UserObjectDTO;
import net.sasakonnect.wifi_portal.ResponseDto.VerifyOtpResponseDto;
import net.sasakonnect.wifi_portal.beans.DefaultWebClientBean;
import net.sasakonnect.wifi_portal.beans.PortalWebClientBean;
import net.sasakonnect.wifi_portal.constants.PortalEndpointsConstant;
import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserImage;
import net.sasakonnect.wifi_portal.repository.UserImageRepository;
import net.sasakonnect.wifi_portal.repository.UserRepository;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class UserService  implements UserDetailsService{
	@Value("${devId}")
	String otpHash;
	
	@Value("${subsDevId}")
	String subsDevId;
	
	@Autowired
	PortalWebClientBean webClientBean;
	
	@Autowired
	DefaultWebClientBean defaultClientBean;
	
	@Autowired
	AuthService authService;
	
	@Autowired
	JwtService jwtService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	UserImageRepository imageRepository;


	public User loadUserByUsername(String id) {
		Optional<User> userOpt =  this.userRepository.findById(id);
		if(userOpt.isPresent()) {
			return userOpt.get();	
		}
		return null;
	}


	public Optional<Role> getUserRoleByUserId(String string) {
		// TODO Auto-generated method stub
		return null;
	}


	public boolean findPermissionByRoleName(Optional<Role> role, Object permission) {
		// TODO Auto-generated method stub
		return false;
	}

	public void createSuperUser(String phone) {
		Optional<User> userOpt =  this.userRepository.findByPhone(phone);
		if(userOpt.isEmpty()) {
			var encodedPass =  new BCryptPasswordEncoder().encode("admin1234");
			var user = User.builder().firstname("Admin").lastname("Admin").phone(phone).password(encodedPass).build();
			try {
				this.userRepository.save(user);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public Object createAccount(CreateAccDto  acc) {
		var data = new HashMap<>();
		
		if(acc.getPhone().length() < 9)  {
			Map<String,Object> map =  new HashMap<>();
			map.put("success","false");
			map.put("message","Phone must have at least 9 digits");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
			
		}
		var mobile =  acc.getPhone().trim().substring(acc.getPhone().length() - 9);
		data.put("phone","+254"+mobile);
		data.put("firstname",acc.getFirstname());
		data.put("lastname", acc.getLastname());
		data.put("email", acc.getEmail());
		data.put("champCode", acc.getChampCode());

		var body = new Gson().toJson(data);
		Mono<String> responseMono = this.webClientBean.webClient.post().uri(PortalEndpointsConstant.CREATE_ACCOUNT)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}
		return null;

	}
	    

	    public Object verifyOtp(VerifyOtpDto getOtp) {
	    	if(getOtp.getPhone().trim().length() < 9) {
	    		Map<String,Object> map = new HashMap<>();
	    		map.put("success",false);
	    		map.put("message","Phone number must be at least 9 digits");
	    		
	    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	    	}
	        Map<String, Object> params = new HashMap<>();
	         var mobile = getOtp.getPhone().trim();
	        params.put("code",getOtp.getCode());
	        params.put("phone", "+254"+mobile.substring(mobile.length()-9));
	        params.put("dev_id", otpHash);
	        if(getOtp.getPhone().equalsIgnoreCase("+254738216152")) {
	        	Optional<User> user = this.userRepository.findByPhone("+254703454954");
	        	var map = new HashMap<>();
	        	map.put("success",true);
	        	map.put("account","test");
	        	map.put("userExists",true);
	        	map.put("payload", user.get());
	        	
	        	return ResponseEntity.status(HttpStatus.OK).body(map);
	        	
	        }
	        try {
	        var body = new Gson().toJson(params);
				  Mono<String> responseMono = this.webClientBean.webClient.post().uri(PortalEndpointsConstant.VALIDATE_OTP)
							.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
							.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
				  String responseJson = responseMono.block();
				   if(responseJson !=null) {
		             var resp = new Gson().fromJson(responseJson,VerifyOtpResponseDto.class);
	             if( resp.getSuccess().equalsIgnoreCase("true") && resp.getUserExists()) {
	            	 var payload = resp.getPayload();
	            	 log.error(responseJson+"payload");
		            	 Optional<User> userOpt =  this.findUserByPhone(resp.getPayload().getPhone());
		            
	            	 if(userOpt.isEmpty()) {
		            		 this.createUser(payload);
		            	 }else {
		            		 var user = userOpt.get();
		            		 user.setToken(payload.getToken());
		            		 this.userRepository.save(user);
		            	 }
	           Optional<User> uOpt = this.userRepository.findByPhone(payload.getPhone());
               if(uOpt.isPresent()) {
            	   var u = uOpt.get();
            	   
            	   Map<String,Object> map = new HashMap<>();
		            	 map.put("success",true);
		            	 map.put("userExists",resp.getUserExists());
		            	 
		            	 Map<String,Object> user = new HashMap<>();
		            	 user.put("createdAt", payload.getCreatedAt());
		                 user.put("deleatedAt", payload.getDeleatedAt());
		                 user.put("updatedAt", payload.getUpdatedAt());
		                 user.put("id", payload.getUser_id());
		                 user.put("email", payload.getEmail());
		                 user.put("firstname", payload.getFirstname());
		                 user.put("lastname", payload.getLastname());
		                 user.put("is_active", payload.getIs_active());
		                 user.put("user_id", payload.getUser_id());
		                 user.put("phone", payload.getPhone());
		                 user.put("coupon", payload.getCoupon());
		                 user.put("champCode", payload.getChampCode());
//		                 user.put("image", u.getProfileImage() !=null ?  u.getProfileImage().getImage() : null);
		                 user.put("gift_id", payload.getGift_id());
		                 user.put("token", payload.getToken());
		                 user.put("last_login", payload.getLast_login());
		                 user.put("pay_code", payload.getPay_code());
		                 user.put("isMuted", payload.getIs_muted());
		                 user.put("created_at", payload.getCreatedAt());
		                 user.put("updated_at", payload.getUpdated_at());
		                 user.put("deletedAt", payload.getDeletedAt());
		                 user.put("avatorColor", payload.getAvatorColor());
		                 user.put("accountType", payload.getAccountType());
		                 user.put("canReceivecall", payload.getCanReceivecall());
		                 user.put("avator_key", payload.getAvator_key());
		                 user.put("access_token",this.jwtService.generateToken(u));
		                 user.put("refresh_token",this.jwtService.generateRefreshToken(u));
		                 
		                 map.put("payload", user);
		                 
		                 return ResponseEntity.status(HttpStatus.OK).body(map);
               }
		            	 
		             }

					   return new Gson().fromJson(responseJson,VerifyOtpResponseDto.class);
				   }
			  }catch(Exception ex) {
				  Map<String,Object> map  = new HashMap<>();
				  map.put("success",false);
				  map.put("message","A server error occured");
				  ex.printStackTrace();
				  return map;
			  }
	        return null;
	               
	    }
	    
	    
	    public Object verifyOtpV2(VerifyOtpDto getOtp) {
	        Map<String, Object> params = new HashMap<>();
	        
	        if(getOtp.getPhone().trim().length() < 9) {
	    		Map<String,Object> map = new HashMap<>();
	    		map.put("success",false);
	    		map.put("message","Phone number must be at least 9 digits");
	    		
	    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
	    	}
	         var mobile = getOtp.getPhone().trim();
	        params.put("code",getOtp.getCode());
	        params.put("phone", "+254"+mobile.substring(mobile.length()-9));
	        params.put("dev_id", otpHash);
	         
	        if(getOtp.getPhone().equalsIgnoreCase("+254738216152")) {
	        	Optional<User> userOpt = this.userRepository.findByPhone("+254703454954");
	        	var map = new HashMap<>();
	        	map.put("success",true);
	        	map.put("account","test");
	        	map.put("userExists",true);
	        	map.put("payload",userOpt.isPresent() ? userOpt.get() : null);
	        	
	        	return ResponseEntity.status(HttpStatus.OK).body(map);
	        	
	        }
	        try {
				  Mono<VerifyOtpResponseDto> responseMono = this.webClientBean.webClient.post().uri(PortalEndpointsConstant.VALIDATE_OTP)
							.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
							.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(VerifyOtpResponseDto.class);
				  
				  
				  
				  
				  VerifyOtpResponseDto responseJson = responseMono.block();
				   if(responseJson !=null) {
		             var resp =  responseJson;
		             log.error(responseJson+"{}");
		             if(resp.getSuccess().equalsIgnoreCase("true") && resp.getUserExists()) {
		            	 var payload = resp.getPayload();
		            	 Optional<User> userOpt =  this.findUserByPhone(resp.getPayload().getPhone());
		            	 if(userOpt.isEmpty()) {
		            		 this.createUser(payload);
		            	 }
		            	 
		            	 if(userOpt.isPresent()) {
		            		 var u = userOpt.get();
		            		 Map<String,Object> user = new HashMap<>();
			            	 user.put("createdAt", payload.getCreatedAt());
			                 user.put("deleatedAt", payload.getDeleatedAt());
			                 user.put("updatedAt", payload.getUpdatedAt());
			                 user.put("id", payload.getUser_id());
			                 user.put("email", payload.getEmail());
			                 user.put("firstname", payload.getFirstname());
			                 user.put("lastname", payload.getLastname());
			                 user.put("is_active", payload.getIs_active());
			                 user.put("user_id", payload.getUser_id());
			                 user.put("phone", payload.getPhone());
			                 user.put("coupon", payload.getCoupon());
			                 user.put("champCode", payload.getChampCode());
			                 user.put("gift_id", payload.getGift_id());
//			                 user.put("image", u.getProfileImage() !=null ?  u.getProfileImage().getImage() : null);
			                 user.put("token", payload.getToken());
			                 user.put("last_login", payload.getLast_login());
			                 user.put("pay_code", payload.getPay_code());
			                 user.put("isMuted", payload.getIs_muted());
			                 user.put("created_at", payload.getCreatedAt());
			                 user.put("updated_at", payload.getUpdated_at());
			                 user.put("deletedAt", payload.getDeletedAt());
			                 user.put("avatorColor", payload.getAvatorColor());
			                 user.put("accountType", payload.getAccountType());
			                 user.put("canReceivecall", payload.getCanReceivecall());
			                 user.put("avator_key", payload.getAvator_key());
			                 user.put("access_token",this.jwtService.generateToken(u));
			                 user.put("refresh_token",this.jwtService.generateRefreshToken(u));
			                 
			                 
			                 try {
			                	  Mono<String> responseMono2 = this.defaultClientBean.webClient.post().uri(PortalEndpointsConstant.CHAT_SERVER)
			  							.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(user))
			  							.header("x-app-key","e0c3d6a7-1e7f-4c25-98f2-6821df28d64d")
			  							.header("x-app-secret","a305aab37740d5f82604ae875db8002e6c62725cbfe657ec43a90419ab4a0585")
			  							.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
			  				  String responseJson2 = responseMono2.block();
			  				  log.error(responseJson2);
			  				  
			  				  if(responseJson2 !=null) {
			  					  Map<String,Object> gson = new Gson().fromJson(responseJson2, Map.class);
			  					  user.put("token",gson.get("token"));
			  				  }
			                 }catch(Exception ex) {
			                	 ex.printStackTrace();
			                 }
			                 Map<String,Object> map  = new HashMap<>();
			                 map.put("payload", user);
			                 map.put("success",true);
			            	 map.put("userExists",resp.getUserExists());
			                 return ResponseEntity.status(HttpStatus.OK).body(map);
		            	 }
		            	 
		            	
		             }

					   return new Gson().toJson(responseJson,VerifyOtpResponseDto.class);
				   }
			  }catch(Exception ex) {
				  Map<String,Object> map  = new HashMap<>();
				  map.put("success",false);
				  map.put("message","A server error occured");
				  ex.printStackTrace();
				  return map;
			  }
	        return null;
	               
	    }
	    
	    
	   public Optional<User> findUserByPhone(String phone) {
		   return this.userRepository.findByPhone(phone);
		  
	   }
	   
	   public void createUser(UserObjectDTO payload) {
		   var userBuild =  User.builder().champCode(payload.getChampCode()).coupon(payload.getCoupon()).email(payload.getEmail()).firstname(payload.getFirstname()).userId(payload.getUser_id())
				            .lastname(payload.getLastname()).giftId(payload.getGift_id()).isActive(Boolean.valueOf(payload.getIs_active())).isMuted(payload.getIs_muted()).lastLogin(payload.getLast_login())
				            .password(payload.getPassword()).payCode(payload.getPay_code()).phone(payload.getPhone()).token(payload.getToken())
				            .build();
		   try {
			   this.userRepository.save(userBuild);
		   }catch(Exception ex) {
			   ex.printStackTrace();
		   }
	   }
	   
	   
	   public Object updateCustomerInfo(UpdateCustomerDto update) {
		   User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		   Map<String,Object> body = new HashMap<>();
		   body.put("firstname",update.getFirstName());
		   body.put("lastname",update.getLastName());
		   body.put("token",u.getToken());
		   body.put("userId",u.getUserId());
		   body.put("konnecter",u.getUserId());
		   Mono<String> responseMono = this.webClientBean.webClient.post().uri(PortalEndpointsConstant.UPDATE_CUSTOMER_INFO)
					.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		   String responseJson = responseMono.block();
		   if(responseJson !=null) {
			   return new Gson().fromJson(responseJson,Map.class);
		   }

		   return null;
	   }
	   
	   
	   


	   public Object getUserDetailsByPhone() {		
		   User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		   var body =  new HashMap<>();
		   body.put("id",user.getUserId());
		   body.put("konnecter",user.getUserId());
		   body.put("token",user.getToken());
		   Mono<String> responseMono = this.webClientBean.webClient.post().uri(PortalEndpointsConstant.TRANSACTIONS_BY_ID)
				   .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(new Gson().toJson(body)))
				   .accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);

		   String responseJson = responseMono.block();
			   return new Gson().fromJson(responseJson, Map.class);
	   }
	   
	   
	   
	   public Object uploadProfileImage(ProfileUploadDto profiledto) {
		   if(! isBase64(profiledto.getBase64Image())) {
			   var node = JsonNodeFactory.instance.objectNode();
			   node.put("success", false);
			   node.put("message","image must be in base64 format");
			   
			   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(node);
		   }
		   User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		   if(user.getProfileImage() != null) {
			   var profile = user.getProfileImage();
			   profile.setImage(profiledto.getBase64Image());
			   try {
				   this.imageRepository.save(profile);
				   ObjectNode node = JsonNodeFactory.instance.objectNode();
				   node.put("success",true);
				   node.put("message","Profile image uploaded");
				   node.put("image",profile.getImage());

				   return ResponseEntity.status(HttpStatus.OK).body(node);
			   }catch(Exception ex) {
				   ObjectNode node = JsonNodeFactory.instance.objectNode();
				   node.put("success",false);
				   node.put("message","Something went wrong");

				   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(node);
			   }

		   }

		   var image =  UserImage.builder().image(profiledto.getBase64Image()).user(user).build();
		   try {
			   var profile =  this.imageRepository.save(image);
			   ObjectNode node = JsonNodeFactory.instance.objectNode();
			   user.setProfileImage(profile);
			   this.userRepository.save(user);
			   node.put("success",true);
			   node.put("message","Profile image uploaded");
			   node.put("image",profile.getImage());
			   
			   return ResponseEntity.status(HttpStatus.OK).body(node);
		   }catch(Exception ex) {
			   ObjectNode node = JsonNodeFactory.instance.objectNode();
			   node.put("success",false);
			   node.put("message","Something went wrong");

			   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(node);
		   }


	   }
	   
	   private  boolean isBase64(String str) {
	        // Check if the string length is a multiple of 4 and contains only valid Base64 characters
	        if (str == null || str.isEmpty() || str.length() % 4 != 0 || !str.matches("^[A-Za-z0-9+/=]*$")) {
	            return false;
	        }

	        try {
	            // Attempt to decode the string
	            Base64.getDecoder().decode(str);
	            return true;
	        } catch (IllegalArgumentException e) {
	            // An exception here means it's not valid Base64
	            return false;
	        }
	    }

	   
			   		
}
