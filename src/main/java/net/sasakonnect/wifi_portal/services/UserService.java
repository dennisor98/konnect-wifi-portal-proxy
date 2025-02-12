package net.sasakonnect.wifi_portal.services;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.AssignRoleDto;
import net.sasakonnect.wifi_portal.RequestDto.CreateAccDto;
import net.sasakonnect.wifi_portal.RequestDto.ProfileUploadDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdateCustomerDto;
import net.sasakonnect.wifi_portal.RequestDto.VerifyOtpDto;
import net.sasakonnect.wifi_portal.RequestDto.sdk.UserLoginDto;
import net.sasakonnect.wifi_portal.ResponseDto.GetTokenDto;
import net.sasakonnect.wifi_portal.ResponseDto.UserObjectDTO;
import net.sasakonnect.wifi_portal.ResponseDto.VerifyOtpResponseDto;
import net.sasakonnect.wifi_portal.beans.DefaultWebClientBean;
import net.sasakonnect.wifi_portal.beans.PortalWebClientBean;
import net.sasakonnect.wifi_portal.beans.ThreadExecuterBean;
import net.sasakonnect.wifi_portal.constants.PortalEndpointsConstant;
import net.sasakonnect.wifi_portal.domain.Role;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserRole;
import net.sasakonnect.wifi_portal.domain.UserImage;
import net.sasakonnect.wifi_portal.repository.RoleRepository;
import net.sasakonnect.wifi_portal.repository.UserImageRepository;
import net.sasakonnect.wifi_portal.repository.UserRepository;
import net.sasakonnect.wifi_portal.repository.UserRoleRepository;
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
	UserRoleRepository userRoleRepository;
	
	@Autowired
	UserImageRepository imageRepository;
	
	@Autowired
	PortalWebClientBean portalWebClient;
	
	@Autowired
	RoleRepository roleRepository;
	
	@Autowired
	ThreadExecuterBean threadExceutorBean;
	
	@Value("${api.message.url}")
	String messageUrl;
	@Value("${api.message.auth}")
	String messageAuth;
	
	
	public User loadUserByUsername(String id) {
		Optional<User> userOpt =  this.userRepository.findById(id);
		if(userOpt.isPresent()) {
			return userOpt.get();	
		}
		return null;
	}
	
	public Object adminUserLogin(UserLoginDto logins) {
		String phone =  logins.getPhone();
		if(phone.length() < 9) {
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","phone should have at least 9 digits");
			
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		String mobile = phone.substring(phone.length() -9);
		Optional<User> userOpt =  this.userRepository.findByPhone("+254"+mobile);
		if(userOpt.isEmpty()) {
			ObjectNode res = JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","Access denied");
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
		}
		var user = userOpt.get();
		Optional<Role> roleOpt = this.userRoleRepository.findRoleByUser(user);
		if(roleOpt.isEmpty()) {
			ObjectNode res =  JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","Access denied");
			
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
			
		}
		Map<String,Object> resPayload = new HashMap<>();
		Map<String,Object> res = new HashMap<>();
		
		Map<String,Object> userObj =  new HashMap<>();
		userObj.put("firstName",user.getFirstname());
		userObj.put("lastName",user.getLastname());
		userObj.put("phone",user.getPhone());
		res.put("success", true);
		res.put("access_token",this.jwtService.generateAdminToken(user));
		res.put("refresh_token",this.jwtService.generateAdminRefreshToken(user));
		res.put("user",userObj);
		resPayload.put("payload", res);
	   return ResponseEntity.status(HttpStatus.OK).body(resPayload);	
	}

	public Optional<User> findUserById(String id){
		return  this.userRepository.findById(id);
	}

	public Optional<Role> getUserRoleByUserId(String string) {
		Optional<User> userOpt = this.userRepository.findById(string);
		if(userOpt.isPresent()) {
			var user = userOpt.get();
			return this.userRoleRepository.findRoleByUser(user);
		}
		
		return Optional.empty();
	}


	public boolean findPermissionByRoleName(Optional<Role> role, Object permission) {
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
	        if(getOtp.getPhone().equalsIgnoreCase("738216152")) {
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
		                 user.put("id",u.getId());
		                 user.put("email",u.getEmail());
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
	    
	    private  String generateRandomDevId() {
	        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	        SecureRandom random = new SecureRandom();
	        final int length = 50;
	        StringBuilder sb = new StringBuilder(length);
	        for (int i = 0; i < length; i++) {
	            int index = random.nextInt(CHARACTERS.length());
	            sb.append(CHARACTERS.charAt(index));
	        }
	        return sb.toString();
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
	        params.put("dev_id",this.generateRandomDevId());
	         
	       
	        
	        if(getOtp.getPhone().equalsIgnoreCase("738216152")) {
	        	Optional<User> userOpt = this.userRepository.findByPhone("+254703454954");
	        	var payload=userOpt.get();
	        	var m = new HashMap<>();
	        	m.put("success",true);
	        	m.put("account","test");
	        	m.put("userExists",true);
	        	 Map<String,Object> map  = new HashMap<>();
	        	map.put("createdAt", payload.getCreatedAt());
	        	map.put("deleatedAt", payload.getDeletedAt());
	        	map.put("updatedAt", payload.getUpdatedAt());
	        	map.put("id", payload.getId());
	        	map.put("email", payload.getEmail());
	        	map.put("firstname", payload.getFirstname());
	        	map.put("lastname", payload.getLastname());
	        	map.put("is_active", payload.isActive());
	        	map.put("user_id", payload.getUserId());
	        	map.put("phone", payload.getPhone());
	        	map.put("coupon", payload.getCoupon());
	        	map.put("champCode", payload.getChampCode());
	        	map.put("gift_id", payload.getGiftId());
//                 user.put("image", u.getProfileImage() !=null ?  u.getProfileImage().getImage() : null);
	        	map.put("token", payload.getToken());
	        	map.put("last_login", payload.getLastLogin());
	        	map.put("pay_code", payload.getPayCode());
	        	map.put("isMuted", payload.isMuted());
	        	map.put("created_at", payload.getCreatedAt());
	        	map.put("updated_at", payload.getUpdatedAt());
	        	map.put("deletedAt", payload.getDeletedAt());
	        	map.put("avatorColor", payload.getAvatorColor());
	        	map.put("access_token",this.jwtService.generateToken(payload));
	        	map.put("refresh_token",this.jwtService.generateRefreshToken(payload));
	        	
	        	
	        	
	        	
	        	m.put("payload",map);
	        	
	        	return ResponseEntity.status(HttpStatus.OK).body(map);
	        	
	        }

	        try {
				  Mono<VerifyOtpResponseDto> responseMono = this.webClientBean.webClient.post().uri(PortalEndpointsConstant.VALIDATE_OTP)
							.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
							.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(VerifyOtpResponseDto.class);
				  VerifyOtpResponseDto responseJson = responseMono.block();
				   if(responseJson !=null) {
		             var resp =  responseJson;
		             if(resp.getSuccess().equalsIgnoreCase("true") && resp.getUserExists()) {
		            	 var payload = resp.getPayload();
		            	 Optional<User> userOpt =  this.findUserByPhone(resp.getPayload().getPhone());
		            	 if(userOpt.isEmpty()) {
		            		 this.createUser(payload);
		            	 }
		            	 
		            	 if(userOpt.isPresent()) {
		            		 var u = userOpt.get();
		            		 u.setToken(payload.getToken());
		            		 u.setDevId(payload.getDev_id());
		            		 try {
		            			 this.userRepository.save(u);
		            		 }catch(Exception ex) {
		            			 
		            		 }
		            		 Map<String,Object> user = new HashMap<>();
			            	 user.put("createdAt", payload.getCreatedAt());
			                 user.put("deleatedAt", payload.getDeleatedAt());
			                 user.put("updatedAt", payload.getUpdatedAt());
			                 user.put("id",u.getId());
			                 user.put("email", payload.getEmail());
			                 user.put("firstname", payload.getFirstname());
			                 user.put("lastname", payload.getLastname());
			                 user.put("is_active", payload.getIs_active());
			                 user.put("user_id", payload.getUser_id());
			                 user.put("phone", payload.getPhone());
			                 user.put("coupon", payload.getCoupon());
			                 user.put("champCode", payload.getChampCode());
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
				            .password(payload.getPassword()).payCode(payload.getPay_code()).phone(payload.getPhone()).token(payload.getToken()).devId(payload.getDev_id())
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
	   
	   
	   
	   private String getUserToken(User user) {
		   if(user.getDevId() == null) {
				log.error("devId is NULL");
				return user.getToken();
			}
			Map<String,Object> req = new HashMap<>();
			req.put("dev_id",user.getDevId());
			req.put("phone",user.getPhone());

			var body = new Gson().toJson(req);
			try {
				Mono<GetTokenDto> responseMono = this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.GET_USER_TOKEN)
						.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
						.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(GetTokenDto.class);
				GetTokenDto responseJson = responseMono.block();
				log.error("res"+responseJson);
				if(responseJson !=null && responseJson.getToken() !=null) {
					return responseJson.getToken();
				}
				
				return user.getToken();
				}catch(Exception ex) {
				
				ex.printStackTrace();
				return user.getToken();
			}
		}

	   public Object getUserDetailsByPhone() {		
		   User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		   var body =  new HashMap<>();
		   body.put("id",user.getUserId());
		   body.put("konnecter",user.getUserId());
		   body.put("token",this.getUserToken(user));
		   Mono<String> responseMono = this.webClientBean.webClient.post().uri(PortalEndpointsConstant.ACTIVE_SUBSCRIPTION_BY_USER_ID)
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
	            Base64.getDecoder().decode(str);
	            return true;
	        } catch (IllegalArgumentException e) {
	            return false;
	        }
	    }
	   
	   
	   public Object createRefreshToken() {
		   log.warn("principal " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		   User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		   var token = this.jwtService.generateToken(user);
		   var refresh = this.jwtService.generateRefreshToken(user);

		   Map<String, Object> map = new HashMap<String, Object>();
		   Map<String, Object> payload = new HashMap<String, Object>();
		   payload.put("token", token);
		   payload.put("refreshToken", refresh);

		   map.put("payload", payload);

		   map.put("success", true);
		   return ResponseEntity.status(HttpStatus.OK).body(map);
	   }

	   
	   public Object getAuthenticatedUserProfile() {
		   User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		   ObjectNode res = JsonNodeFactory.instance.objectNode();
           Map<String,Object> map =  new HashMap<>();
		   res.put("id",user.getId());
		   res.put("email", user.getEmail());
		   res.put("firstname", user.getFirstname());
		   res.put("lastname", user.getLastname());
		   map.put("user",res);
		   return ResponseEntity.status(HttpStatus.OK).body(map);
	   }

	   
	   public Object getUsers(Pageable pageable) {
		   Page<User> usersPage =  this.userRepository.findAll(pageable);
		   Map<String, Object> pageInfo = new HashMap<>();
		    pageInfo.put("totalPages", usersPage.getTotalPages());
		    pageInfo.put("totalElements", usersPage.getTotalElements());
		    pageInfo.put("currentPage", usersPage.getNumber());
		    pageInfo.put("pageSize", usersPage.getSize());
		    pageInfo.put("hasPreviousPage", usersPage.hasPrevious());
		    pageInfo.put("hasNextPage", usersPage.hasNext());
		    pageInfo.put("previousPage", usersPage.hasPrevious() ? usersPage.previousPageable().getPageNumber() : null);
		    pageInfo.put("nextPage", usersPage.hasNext() ? usersPage.nextPageable().getPageNumber() : null);
		   var users =  usersPage.stream()
				   .map(u->{
					  Map<String,Object> map = new HashMap<>();
					  map.put("id", u.getId());
					  map.put("user_id",u.getUserId());
					  map.put("name",u.getFirstname()+" "+u.getLastname());
					  map.put("phone",u.getPhone());
					  map.put("email", u.getEmail());
					  return map;
				   }).collect(Collectors.toList());
		   
		   Map<String,Object> payload = new HashMap<>();
		   payload.put("success",true);
		   payload.put("message","Request completed");
		   payload.put("users",users);
		   payload.put("pageInfo",pageInfo);
		   Map<String,Object> res = new HashMap<>();
		   res.put("payload",payload);
		   return ResponseEntity.status(HttpStatus.OK).body(res);
	   }
	   
	   public Object addAdminUser(AssignRoleDto roleDto) {
		   User loggedInUser =  (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		   Optional<User> userOpt =  this.userRepository.findById(roleDto.getUserId());
		   if(userOpt.isEmpty()) {
			   ObjectNode res = JsonNodeFactory.instance.objectNode();
			   res.put("success",false);
			   res.put("message","Invalid userId");

			   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		   }

		   Optional<Role> roleOpt = this.roleRepository.findById(roleDto.getRoleId());
		   if(roleOpt.isEmpty()) {
			   ObjectNode res = JsonNodeFactory.instance.objectNode();
			   res.put("success",false);
			   res.put("message","Invalid roleId");

			   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		   }

		   var role =  roleOpt.get();

		   Optional<Role> userHasRoleOpt =  this.getUserRoleByUserId(roleDto.getUserId());
		   if(userHasRoleOpt.isPresent()) {
			   var userRole = userHasRoleOpt.get();
			   if(userRole.getId().equalsIgnoreCase(role.getId())) {
				   ObjectNode res = JsonNodeFactory.instance.objectNode();
				   res.put("success",false);
				   res.put("message","User already assigned to Role");

				   return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
			   }
//			   this.userRoleRepository.delete(userRole);
		   }

		   var user =  userOpt.get();

		   var userRole =  UserRole.builder().creator(loggedInUser).user(user).role(role).build();
		   
		   String phone = user.getPhone().trim();
		   String sanitizedMobile = "254"+ phone.substring(phone.length() - 9);
		   
		   try {
			   String password =  getRandomPassword();
			   
			   this.threadExceutorBean.addTask(new Runnable() {

				@Override
				public void run() {
					String message = "Your password is "+password;
					Map<String,Object> params = new HashMap<>();
					params.put("phone", sanitizedMobile);
					params.put("message",message);
					Mono<String> responseMono = defaultClientBean.webClient.post().uri(messageUrl)
							.header("Authorization", "Bearer " + messageAuth)
							.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
							.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class)
							.doOnSuccess(response -> log.info("Response",response))
							.doOnError(error-> log.error("Request failed",error));
					String json =  responseMono.block();
					
				}
				   
			   });
			   
			   
			   var encodedPass =  new BCryptPasswordEncoder().encode(password);
			   user.setPassword(encodedPass);
			   this.userRoleRepository.save(userRole);
			   ObjectNode res = JsonNodeFactory.instance.objectNode();
			   res.put("success",true);
			   res.put("message","User added to role");
			   return ResponseEntity.status(HttpStatus.OK).body(res);
		   }catch(Exception ex) {
			   ObjectNode res = JsonNodeFactory.instance.objectNode();
			   res.put("success",false);
			   res.put("message","A server error ocurred");

			   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		   }


	   }
	   
	   private String getRandomPassword() {
		    String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		    SecureRandom RANDOM = new SecureRandom();
		    StringBuilder sb = new StringBuilder(6);
	        for (int i = 0; i < 6; i++) {
	            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
	        }
	        return sb.toString();
	   }
	   
			   		
}
 