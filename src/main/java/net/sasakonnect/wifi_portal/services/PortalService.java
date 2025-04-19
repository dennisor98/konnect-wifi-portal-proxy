package net.sasakonnect.wifi_portal.services;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice.This;
import net.sasakonnect.wifi_portal.RequestDto.AddDeviceDto;
import net.sasakonnect.wifi_portal.RequestDto.ChangeDeviceDto;
import net.sasakonnect.wifi_portal.RequestDto.ConnectTvDto;
import net.sasakonnect.wifi_portal.RequestDto.KompVlanDto;
import net.sasakonnect.wifi_portal.RequestDto.PackageByMacDto;
import net.sasakonnect.wifi_portal.RequestDto.PollMpesaDto;
import net.sasakonnect.wifi_portal.RequestDto.SaveTvConnectDto;
import net.sasakonnect.wifi_portal.RequestDto.SendOtpDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDtoV1;
import net.sasakonnect.wifi_portal.RequestDto.TillConfirmDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdatePackageDto;
import net.sasakonnect.wifi_portal.ResponseDto.InternetPackageDto;
import net.sasakonnect.wifi_portal.ResponseDto.PackageResponseDto;
import net.sasakonnect.wifi_portal.ResponseDto.GetTokenDto;
import net.sasakonnect.wifi_portal.beans.AdvancedUniqueKeyGenerator;
import net.sasakonnect.wifi_portal.beans.DefaultWebClientBean;
import net.sasakonnect.wifi_portal.beans.PortalWebClientBean;
import net.sasakonnect.wifi_portal.beans.ThreadExecuterBean;
import net.sasakonnect.wifi_portal.constants.PortalEndpointsConstant;
import net.sasakonnect.wifi_portal.domain.InternetPackages;
import net.sasakonnect.wifi_portal.domain.TvConnection;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.VirtualSub;
import net.sasakonnect.wifi_portal.repository.InternetPackageRepository;
import net.sasakonnect.wifi_portal.repository.PaymentRepository;
import net.sasakonnect.wifi_portal.repository.TvConnectionRepository;
import net.sasakonnect.wifi_portal.repository.UserDevicesRepository;
import net.sasakonnect.wifi_portal.repository.UserRepository;
import net.sasakonnect.wifi_portal.repository.VirtualSubRepository;
import reactor.core.publisher.Mono;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

@Service
@Slf4j
public class PortalService {

	@Autowired
	PortalWebClientBean portalWebClient;

	@Autowired
	DefaultWebClientBean defaultWeclientBean;

	@Value("${portalUserName}")
	private String portalUserName;
	@Value("${devId}")
	String devId;
	@Value("${portalUserPassword}")
	private String portalUserPassword;
	@Autowired
	InternetPackageRepository packageRepository;
	@Autowired
	AuthService authService;
	@Value("${subsDevId}")
	private String subsDevId;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	UserService userService;
	@Autowired
	PnalService pnalService;
	@Autowired
	PaymentService paymentService;
	@Autowired
	PaymentRepository paymentRepository;
	@Autowired
	TvConnectionRepository tvconnectRepository;
	@Autowired
	ThreadExecuterBean threadExceutorBean;
	@Autowired
	VirtualSubRepository vsubRepository;
	
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
			if(responseJson !=null && responseJson.getToken() !=null) {
				return responseJson.getToken();
			}
		}catch(Exception ex) {

			ex.printStackTrace();
			return user.getToken();
		}
		return user.getToken();
	}
	
	public Object getDevices() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String,Object> req = new HashMap<>();
		req.put("id",user.getUserId());
		req.put("konnecter",user.getUserId());
		req.put("token",this.getUserToken(user));

		var body = new Gson().toJson(req);
		try {
			Mono<String> responseMono = this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.GET_ACTIVE_DEVICE)
					.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
			String responseJson = responseMono.block();
			if(responseJson !=null) {
				var resp = new Gson().fromJson(responseJson,Map.class);
				return resp;
			}
		}catch(Exception ex) {
			Map<String,Object> map  = new HashMap<>();
			map.put("success",false);
			map.put("message","A server error occured");
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(map);
		}
		return null;
	}


	public Object getInternetPackages() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String,Object> map = new HashMap<>();
		Mono<PackageResponseDto> responseMono = this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.GET_PACKAGES)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(new Gson().toJson(map)))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(PackageResponseDto.class);
		PackageResponseDto responseJson = responseMono.block();
		if(responseJson !=null) {
			var resp = responseJson;
			Map<String,Object> res =  new HashMap<>();
			res.put("success",resp.getSuccess());
			res.put("payload", resp.getPayload());
			this.updatePackages(resp.getPayload());
			
		}
		List<VirtualSub> vsubLists =  this.pnalService.getUserVirtualSub();
		List<InternetPackages> packagesList = this.packageRepository.findAll(Sort.by(Sort.Direction.ASC,"cost"));
		var packages = packagesList.stream()
				.filter(p -> {
			        if (p.getIsGift() && vsubLists.isEmpty()) {
			            return false;
			        }
			        return true;
			    })
				.map(p->{
					Map<String,Object> pkgs = new HashMap<>();
					pkgs.put("createdAt",String.valueOf(p.getCreatedAt()));
					pkgs.put("updated_at",String.valueOf(p.getUpdatedAt()));
					pkgs.put("deletedAt",String.valueOf(false));
					pkgs.put("id",p.getForeignPackageId());
					pkgs.put("name",p.getName());
					pkgs.put("description",p.getDescription());
					pkgs.put("cost", p.getCost());
					pkgs.put("active", p.getActive());
					pkgs.put("noOfUsers", p.getNoOfUsers());
					pkgs.put("zone", p.getZone());
					pkgs.put("promotionText", p.getPromotionText());
					pkgs.put("onPromotion",p.getOnPromotion());
					return pkgs;
				}).collect(Collectors.toList());

		Map<String,Object> res =  new HashMap<>();
		res.put("payload",packages);
		return ResponseEntity.status(HttpStatus.OK).body(res);

	}


	private void updatePackages(List<InternetPackageDto> data) {
		Thread thread = new Thread(() -> {
			data.stream()
			.map(d -> {
				Optional<InternetPackages> packageOpt = this.packageRepository.findByForeignPackageId(d.getId());
				if (packageOpt.isEmpty()) {
					var pkg = InternetPackages.builder()
							.foreignPackageId(d.getId())
							.active(d.getActive())
							.cost(d.getCost())
							.name(d.getName())
							.noOfUsers(d.getNoOfUsers())
							.zone(d.getZone())
							.build();
					return this.packageRepository.save(pkg);
				} 
				return null;
			})
			.collect(Collectors.toList());
		});

		thread.start(); 
	}

	public Object sendOtp(SendOtpDto login) {
		var phone = login.getPhone().trim();
		if(phone.length() < 9) {
			var map = new HashMap<>();
			map.put("success",false);
			map.put("message","Incorrect phone number");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
		}

		var params = new HashMap<>();
		params.put("phone", "+254"+phone);
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		String devId = null;
		if(attrs !=null) {
			HttpServletRequest request = attrs.getRequest();

			devId = request.getHeader("user-agent");
		}
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.SEND_OTP)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
				.header("user-agent",devId)
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}

		return null;
	}
	public Object reSendOtp(SendOtpDto login) {
		ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

		String devId = null;
		if(attrs !=null) {
			HttpServletRequest request = attrs.getRequest();

			devId = request.getHeader("user-agent");
		}
		var phone = login.getPhone().trim();
		if(phone.length() < 9) {
			var map = new HashMap<>();
			map.put("success",false);
			map.put("message","Incorrect phone number");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
		}

		var params = new HashMap<>();
		params.put("phone", "+254"+phone);

		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.RE_SEND_OTP)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
				.header("user-agent",devId)
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}

		return null;
	}


   @Transactional
	public Object addDevice(AddDeviceDto deviceDto) {		 
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var authAttempt = deviceDto.getAuthAttempt();
		Map<String,Object> auth = new HashMap<>();
		Map<String,String> params = new HashMap<>();
		auth.put("pagetype",authAttempt.getPagetype());
		auth.put("vlan",authAttempt.getVlan());
		auth.put("staMac",authAttempt.getStaMac());
		auth.put("staIp",authAttempt.getStaIp());
		auth.put("apMac",authAttempt.getApMac());
		auth.put("apIp",authAttempt.getApIp());
		auth.put("ssid",authAttempt.getSsid());
		auth.put("acIp",authAttempt.getAcIp());

		params.put("id", deviceDto.getId());
		params.put("code",deviceDto.getCode());
		params.put("token",this.getUserToken(user));
		params.put("userId",user.getUserId());
		params.put("konnecter",user.getUserId());
		params.put("authAttempt",new Gson().toJson(auth));
		Optional<VirtualSub> vsubOpt =  this.pnalService.getVsubByID(deviceDto.getCode());
		if(vsubOpt.isPresent()) {
			var vsub = vsubOpt.get();
			if(!vsub.getIsActive()) {
				Map<String,Object> res = new HashMap<>();
				res.put("success",false);
				res.put("message","Inactive subscription");
				
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
			}
			 threadExceutorBean.addTask(new Runnable() {
				 @Override
		    	 public void run() {
		    		 log.error("Executing task...");
			//make a subscription creation request to portal micro service
			ObjectNode resp =  JsonNodeFactory.instance.objectNode();
		     resp.put("TransType","Merchant Pay Online");
		     resp.put("TransID",pnalService.generateGiftTransId());
		     resp.put("TransAmount",vsub.getAmount());
		     resp.put("TransTime", System.currentTimeMillis());
		     resp.put("BusinessShortCode","5467232");
		     resp.put("packageId",vsub.getPackageId());
		     resp.put("BillRefNumber",pnalService.generateGiftTransId());
		     resp.put("Mobile",user.getPhone());
		     resp.put("name",user.getFirstname()+" "+user.getLastname());
		     resp.put("userId",user.getUserId());
		     resp.put("KonnectTransID",AdvancedUniqueKeyGenerator.generateUniqueKey().toUpperCase());
		     resp.put("ResultCode","0");
		     resp.put("staMac",authAttempt.getStaMac());
		     resp.put("initiator","super-app");
		     log.error(resp+"{body}");
		    
	        
		    	 
		    		 try {
		    			 vsub.setIsActive(false);
		    			 vsubRepository.save(vsub);
		    			 vsubRepository.flush();
		    			 Mono<?> respMono = portalWebClient.webClient.post()
		    					 .uri("https://api.sasakonnect.net/KonnectC2BConfirmationURL")
		    					 .contentType(MediaType.APPLICATION_JSON)
		    					 .body(BodyInserters.fromValue(resp.toPrettyString()))
		    					 .accept(MediaType.APPLICATION_JSON)
		    					 .exchangeToMono(clientResponse -> {
		    						 HttpStatusCode statusCode = clientResponse.statusCode();

		    						

		    						 return clientResponse.bodyToMono(String.class);
		    					 });
		    					

		    			 var response = respMono.block();
		    			
		    			 log.error("Response: {}", response);
		    		 }catch(Exception ex) {
		    			 ex.printStackTrace();
		    		 }
		    	 }
		     });
		}
		
		
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.ADD_DEVICE_TO_PACKAGE)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();

		if(responseJson !=null) {
			var res = new Gson().fromJson(responseJson,Map.class);
			log.error("{response} "+res);
			return res;
		}
		
		Map<String,Object> res = new HashMap<>();
        res.put("success", true);
        res.put("payload",new ArrayList<>());
		return null;
	}

	public Object getClientSubs() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var data =  new HashMap<>();
		data.put("id", user.getUserId());
		data.put("konnecter", user.getUserId());
		data.put("token",this.getUserToken(user));
		data.put("dev_id",user.getDevId());

		var body = new Gson().toJson(data);

		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.TRANSACTIONS)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}
		return null;
	}

	public Object changeDevice(ChangeDeviceDto device) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var data =  new HashMap<>();
		data.put("konnecter", user.getUserId());
		data.put("token",this.getUserToken(user));
		data.put("oldMac",this.formatMacAddress(device.getOldMac()));
		data.put("newMac",this.formatMacAddress(device.getNewMac()));


		var body = new Gson().toJson(data);
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.SWAP_DEVICE)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}

		return null;
	}


	private String formatMacAddress(String macAddress) {
		if (macAddress == null || macAddress.length() < 12) {
			throw new IllegalArgumentException("macAddress must be at least 12 characters long");
		}

		if (macAddress.contains(":")) {
			return macAddress;
		}


		StringBuilder formattedMac = new StringBuilder();
		for (int i = 0; i < macAddress.length(); i += 2) {
			if (i > 0) {
				formattedMac.append(":");
			}
			// Ensure we do not go out of bounds
			if (i + 2 <= macAddress.length()) {
				formattedMac.append(macAddress, i, i + 2);
			}
		}

		return formattedMac.toString().toLowerCase();
	}


	public Object confirmTillPayment(TillConfirmDto tillDto) {
		var data = new HashMap<>();
		data.put("firstname", tillDto.getFirstname());
		data.put("phone", tillDto.getPhone());
		data.put("subscriptionPlanId", tillDto.getSubscriptionPlanId());
		data.put("ipAddress", tillDto.getIpAddress());
		data.put("authAttempt", tillDto.getAuthAttempt());
		data.put("userId", tillDto.getUserId());
		data.put("amount", tillDto.getAmount());
		data.put("smsContent", tillDto.getSmsContent());

		var body = new Gson().toJson(data);
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.CONFIRM_PACKAGE_THOUGH_MPESA_TILL)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}
		return null;
	}

	public Object mpesaStkPush(StkPushDto tillDto) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var data = new HashMap<>();

		String mobile = null;
		if(tillDto.getPhone() !=null) {
			mobile = tillDto.getPhone().trim();
			if(mobile.length() < 9) {
				Map<String,Object> map = new HashMap<>();
				map.put("success", "false");
				map.put("message","Phone must be at least 9 digits");

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
			}
		}


		data.put("firstname", user.getFirstname());
		data.put("phone",mobile !=null ? "+254"+ mobile.substring(mobile.length() -9 ) : user.getPhone().trim());
		data.put("subscriptionPlanId", tillDto.getSubscriptionPlanId());
		data.put("ipAddress","");
		data.put("authAttempt","");
		data.put("userId",user.getUserId());
		data.put("amount","");
		data.put("smsContent","");
		data.put("initiator","super-app");
		var body = new Gson().toJson(data);
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.BUY_PACKAGE_THOUGH_MPESA)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}
		return null;
	}
	
	public Object mpesaStkPush(StkPushDtoV1 tillDto) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var data = new HashMap<>();

		String mobile = null;
		if(tillDto.getPhone() !=null) {
			mobile = tillDto.getPhone().trim();
			if(mobile.length() < 9) {
				Map<String,Object> map = new HashMap<>();
				map.put("success", "false");
				map.put("message","Phone must be at least 9 digits");

				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
			}
		}


		data.put("firstname", user.getFirstname());
		data.put("phone",mobile !=null ? "+254"+ mobile.substring(mobile.length() -9 ) : user.getPhone().trim());
		data.put("subscriptionPlanId", tillDto.getSubscriptionPlanId());
		data.put("ipAddress","");
		data.put("authAttempt","");
		data.put("userId",user.getUserId());
		data.put("amount","");
		data.put("smsContent","");
		data.put("initiator","super-app");
		data.put("actNow",tillDto.getActNow() ? "true":"false");
		var body = new Gson().toJson(data);
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.BUY_PACKAGE_THOUGH_MPESA)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}
		return null;
	}

	public Object getUserSubscriptionsByPhone(String phone) {
		var mobile = "+254"+phone.substring(phone.length() -9);
		Optional<User> useropt = this.userRepository.findByPhone(mobile);
		if(useropt.isPresent()) {
			var user = useropt.get();
			Map<String,Object> data =  new HashMap<>();
			data.put("id",user.getUserId());
			data.put("konnecter",user.getUserId());
			data.put("token",this.getUserToken(user));

			var body = new Gson().toJson(data);

			Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.TRANSACTIONS_BY_ID)
					.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
			String responseJson = responseMono.block();
			if(responseJson !=null) {
			   var res = new Gson().fromJson(responseJson,Map.class);
			   return res;
			}

		}
		return null;
	}

	public Object getUserSubscriptionsByUserId() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var data =  JsonNodeFactory.instance.objectNode();
		data.put("id",user.getUserId());
		data.put("konnecter",user.getUserId());
		data.put("token",this.getUserToken(user));

		log.error("{body}"+data);

        var virtSubs =  this.pnalService.getUserVirtualPackages();
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.TRANSACTIONS_BY_ID)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(data))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			Type type = new TypeToken<Map<String, Object>>(){}.getType();
			Gson gson = new Gson();
			Map<String, Object> res = gson.fromJson(responseJson, type);

			List<Map<String, Object>> payload = (List<Map<String, Object>>) res.get("payload");
			if (payload == null) {
			    payload = new ArrayList<>();
			    res.put("payload", payload);
			}

			payload.addAll(virtSubs);

			return res;
		}


		return null;
	}
	
	

	public Object getUserSubscriptionsByUserId(String phone) {
		Optional<User> userOpt =  this.userRepository.findByPhone(phone);
		if(userOpt.isEmpty()) {
			ObjectNode res =  JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","Invalid phone");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);

		}
		var user = userOpt.get();
		var data =  JsonNodeFactory.instance.objectNode();
		data.put("id",user.getUserId());
		data.put("konnecter",user.getUserId());
		data.put("token",this.getUserToken(user)); 
		log.error("{body}"+data);
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.TRANSACTIONS_BY_ID)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(data))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}


		return null;
	}

	public Object pollMpesa(PollMpesaDto mpesa) {
		Map<String,Object> data =  new HashMap<>();
		data.put("MerchantRequestID", mpesa.getMerchantRequestID());
		data.put("CheckoutRequestID", mpesa.getCheckoutRequestID());
		data.put("ResponseCode",mpesa.getResponseCode());
		data.put("ResponseDescription",mpesa.getResponseDescription());
		data.put("CustomerMessage",mpesa.getCheckoutRequestID());

		var body = new Gson().toJson(data);

		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.POLL_MPESA_FOR_PAYMENT_UPDATE)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}

		return null;
	}

	public Object getSubsByPackageId(PackageByMacDto pkg) {
		var data = new HashMap<>();
		data.put("id", pkg.getId());

		var body =  new Gson().toJson(data);
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.GET_PACKAGE_BY_ID)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}
		return null;
	}









	public String calculateSubnetFromLocalIp(String localIp) {
		String[] parts = localIp.split("\\.");

		if (parts.length >= 2) {
			return parts[0] + "." + parts[1] + ".0.0";
		} else {
			throw new IllegalArgumentException("Invalid IP address format.");
		}
	}

	public String getKompAuthToken() {
		ObjectNode req = JsonNodeFactory.instance.objectNode();
		req.put("email","apiuser@test.com");
		req.put("password","123456");

		Mono<String> responseMono =  this.defaultWeclientBean.webClient.post().uri(PortalEndpointsConstant.GET_KOMP_AUTH_TOKEN)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(req))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);

		String responseJson = responseMono.block();
		if(responseJson !=null) {
			Map<String,Object> resp = new Gson().fromJson(responseJson,Map.class);
			Map<String,Object> data = ( Map<String,Object>) resp.get("data");

			return (String) data.get("token");
		}

		return null;
	}

	public Object getClientHostCridentials() {
		return null;
	}

	public Object updatePackage(UpdatePackageDto payload) {
		log.error(devId);
		Optional<InternetPackages> pkgOpt =  this.packageRepository.findByForeignPackageId(payload.getId());
		if(pkgOpt.isEmpty()) {
			ObjectNode res =  JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","Invalid id");
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
		}
		var pkg = pkgOpt.get();
		pkg.setActive(payload.getActive());
		pkg.setCost(payload.getCost());
		pkg.setName(payload.getName());
		pkg.setNoOfUsers(payload.getNoOfUsers());
		pkg.setOnPromotion(payload.getOnPromotion());
		pkg.setPromotionText(payload.getPromotionText());
		pkg.setDescription(payload.getDescription());

		try {

			this.packageRepository.save(pkg);
			ObjectNode res =  JsonNodeFactory.instance.objectNode();
			res.put("success",true);
			res.put("message","Package edited successfuly");
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			ObjectNode res =  JsonNodeFactory.instance.objectNode();
			res.put("success",false);
			res.put("message","A server error ocurred");
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
	}
	
	public Object getStatSummary() {
		var totalUsers =  this.userRepository.count();
		var activePackages = this.packageRepository.count();
		var completedTx = this.paymentRepository.countByIsSuccessfulTrue();
		var incompleteTx =  this.paymentRepository.countByIsSuccessfulFalse();
		Map<String,Object> stats = new HashMap<>();
		stats.put("users",totalUsers);
		stats.put("activePackages", activePackages);
		stats.put("completedTx", completedTx);
		stats.put("incompleteTx", incompleteTx);

		Map<String,Object> res = new HashMap<>();
		res.put("success",true);
		res.put("message","Request complete");
		res.put("stats",stats);
		return ResponseEntity.status(HttpStatus.OK).body(res);
	}
	
	public Object getTransactionsTrend() {
		 List<Object[]>  paymentTrends = this.paymentRepository.getWeeklyTransactionSummary();
		 var trends = paymentTrends.stream()
		     .map(p->{
		    	Map<String,Object> trend = new HashMap<>();
		    	trend.put("txDate",p[0]);
		    	trend.put("txCount",p[1]);
		    	trend.put("txAmount",p[2]);
		    return trend;
		     }).collect(Collectors.toList());
		Map<String,Object> trend =  new HashMap<>();
		trend.put("success",true);
		trend.put("summary",trends);
		return ResponseEntity.status(HttpStatus.OK).body(trend);
	}
	
	
	public Object saveTvConnect(SaveTvConnectDto tv) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Optional<TvConnection> deviceOpt = this.tvconnectRepository.findByMacAddress(tv.getStaMac());
		
		//update last connection time if the device connection record exist
		if(deviceOpt.isPresent()) {
			TvConnection device =  deviceOpt.get();
			device.setUpdatedAt(new Date());
			device.setDeviceName(tv.getDeviceName());
			device.setVlan(tv.getVlan());
			
			Map<String,Object> res = new HashMap<>();
			res.put("success",true);
			res.put("message","Connection updated");
			
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}
		// create a new connetion record
		TvConnection connectionBuild = TvConnection.builder().deviceName(tv.getDeviceName()).macAddress(tv.getStaMac()).vlan(tv.getVlan()).user(user).build();
		try {
			this.tvconnectRepository.save(connectionBuild);
			Map<String,Object> res = new HashMap<>();
			res.put("success",true);
			res.put("message","Connection created");
			
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}catch(Exception ex) {
			Map<String,Object> res = new HashMap<>();
			res.put("success",false);
			res.put("message","Error while processing request");
			
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
		}
		
		
	}
	
	
	public Object activateSub(String subID) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String body = "subID="+subID+"&konnecter="+user.getUserId()+"&token="+this.getUserToken(user);
		log.error("{body} "+body);
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.ACTIVATE_SUB)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED).body(BodyInserters.fromValue(body))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();
		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}
		return null;
	}
	
	


}
