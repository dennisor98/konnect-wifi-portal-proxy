package net.sasakonnect.wifi_portal.services;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
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
import net.sasakonnect.wifi_portal.RequestDto.AddDeviceDto;
import net.sasakonnect.wifi_portal.RequestDto.ChangeDeviceDto;
import net.sasakonnect.wifi_portal.RequestDto.ConnectTvDto;
import net.sasakonnect.wifi_portal.RequestDto.KompVlanDto;
import net.sasakonnect.wifi_portal.RequestDto.PackageByMacDto;
import net.sasakonnect.wifi_portal.RequestDto.PollMpesaDto;
import net.sasakonnect.wifi_portal.RequestDto.SendOtpDto;
import net.sasakonnect.wifi_portal.RequestDto.StkPushDto;
import net.sasakonnect.wifi_portal.RequestDto.TillConfirmDto;
import net.sasakonnect.wifi_portal.ResponseDto.InternetPackageDto;
import net.sasakonnect.wifi_portal.ResponseDto.PackageResponseDto;
import net.sasakonnect.wifi_portal.beans.DefaultWebClientBean;
import net.sasakonnect.wifi_portal.beans.PortalWebClientBean;
import net.sasakonnect.wifi_portal.constants.PortalEndpointsConstant;
import net.sasakonnect.wifi_portal.domain.InternetPackages;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.repository.InternetPackageRepository;
import net.sasakonnect.wifi_portal.repository.UserDevicesRepository;
import net.sasakonnect.wifi_portal.repository.UserRepository;
import reactor.core.publisher.Mono;


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

	public Object getDevices() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String,Object> req = new HashMap<>();
		req.put("id",user.getUserId());
		req.put("konnecter",user.getUserId());
		req.put("token",user.getToken());

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
		User u = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		Map<String,Object> map = new HashMap<>();
		Mono<PackageResponseDto> responseMono = this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.GET_PACKAGES)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(new Gson().toJson(map)))
				//				.header("Authorization", "Basic " + authService.getBasicAuth())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(PackageResponseDto.class);
		PackageResponseDto responseJson = responseMono.block();
		if(responseJson !=null) {
			var resp = responseJson;
			Map<String,Object> res =  new HashMap<>();
			res.put("success",resp.getSuccess());
			res.put("payload", resp.getPayload());
			this.updatePackages(resp.getPayload());
			//		   if(resp.getStatus() ==  HttpStatus.OK) {
			//			   
			//		   }
			return ResponseEntity.status(HttpStatus.OK).body(res);
		}
		return null;

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
				} else {
					log.error("exec else");
					var ipg = packageOpt.get();
					ipg.setActive(d.getActive());
					ipg.setCost(d.getCost());
					ipg.setName(d.getName());
					ipg.setNoOfUsers(d.getNoOfUsers());
					ipg.setZone(d.getZone());
					return this.packageRepository.save(ipg);
				}
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



	public Object addDevice(AddDeviceDto deviceDto) {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var authAttempt = deviceDto.getAuthAttempt();
		Map<String,Object> auth = new HashMap<>();
		Map<String,String> params = new HashMap<>();
		auth.put("pagetype",authAttempt.getPageType());
		auth.put("vlan",authAttempt.getVlan());
		auth.put("staMac",authAttempt.getStaMac());
		auth.put("staIp",authAttempt.getStaIp());
		auth.put("apMac",authAttempt.getApMac());
		auth.put("apIp",authAttempt.getApIp());
		auth.put("ssid",authAttempt.getSsid());
		auth.put("acIp",authAttempt.getAcIp());

		params.put("id", deviceDto.getId());
		params.put("code",deviceDto.getCode());
		params.put("token",user.getToken());
		params.put("userId",user.getUserId());
		params.put("konnecter",user.getUserId());
		params.put("authAttempt",new Gson().toJson(auth));
		Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.ADD_DEVICE_TO_PACKAGE)
				.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(params))
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
		String responseJson = responseMono.block();

		if(responseJson !=null) {
			return new Gson().fromJson(responseJson,Map.class);
		}

		return null;
	}

	public Object getClientSubs() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var data =  new HashMap<>();
		data.put("id", user.getUserId());
		data.put("konnecter", user.getUserId());
		data.put("token", user.getToken());
		data.put("dev_id", devId);

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
		data.put("token", user.getToken());
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
			data.put("token",user.getToken());

			var body = new Gson().toJson(data);

			Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.TRANSACTIONS_BY_ID)
					.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
			String responseJson = responseMono.block();
			if(responseJson !=null) {
				return new Gson().fromJson(responseJson,Map.class);
			}

		}
		return null;
	}

	public Object getUserSubscriptionsByUserId() {
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		var data =  JsonNodeFactory.instance.objectNode();
		data.put("id",user.getUserId());
		data.put("konnecter",user.getUserId());
		data.put("token",user.getToken());

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
		data.put("token",user.getToken()); 
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


	public Object getUserObject(String phone) {
		var data = new HashMap<>();
		data.put("phone", phone.trim());
		data.put("dev_id",subsDevId);
		try {
			ObjectMapper mapper = new ObjectMapper(); 
			var body =  mapper.writeValueAsString(data);
			Mono<String> responseMono =  this.portalWebClient.webClient.post().uri(PortalEndpointsConstant.GET_USER_TOKEN)
					.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(body))
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);
			String responseJson = responseMono.block();
			if(responseJson !=null) {
				return new Gson().fromJson(responseJson,Map.class);
			}

		}catch(Exception ex) {

		}


		return null;
	}


	public Object initiateTvConnection(ConnectTvDto tvconnect) {
		String pageType = null;
		if(tvconnect.getVlan() !=null && tvconnect.getMode() !=null) {
			if(tvconnect.getMode().equalsIgnoreCase("gpon")) {
				pageType = "remote";
			}else {
				pageType = "100";
			}
			String requestBody = "pagetype="+pageType+"&vlan="+tvconnect.getVlan()+"&staMac="+tvconnect.getStaMac();
			log.error(requestBody+"{req}");
			Mono<String> responseMono =  this.defaultWeclientBean.webClient.post().uri(PortalEndpointsConstant.WEB_PORTAL_AUTH)

					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.bodyValue(requestBody)
					.header("Authorization","Basic JDJhJDEwJExhQWg1eGhjbzpaMGhLSng1UnZ5bGVHNEhwdkQ3")
					.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);

			String responseJson = responseMono.block();
			if(responseJson !=null) {
				return new Gson().fromJson(responseJson,Map.class);
			}
		}
		//calculate subnet from localIp
		String localIp = tvconnect.getStaIp();

		String subnet = calculateSubnetFromLocalIp(localIp);
		log.error(subnet);
		log.error(subnet);
		if(subnet == null) {
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("success", false);
			node.put("message","Failed to obtain VLAN information");

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(node);
		}
		//request vlan information
		ObjectNode req =  JsonNodeFactory.instance.objectNode();
		req.put("public_ip",tvconnect.getPublicIp());
		req.put("subnet", subnet);
		String uri = UriComponentsBuilder.fromUriString(PortalEndpointsConstant.GET_VLAN_INFO)
				.queryParam("public_ip",tvconnect.getPublicIp())
				.queryParam("subnet",subnet)
				.build()
				.toUriString();

		Mono<String> response =  this.defaultWeclientBean.webClient.get().uri(uri)
				.header("Authorization","Bearer "+getKompAuthToken())
				.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);


		try {
			String responseJson = response.block();
			if(responseJson !=null) {
				var resp = new Gson().fromJson(responseJson,KompVlanDto.class);

				var build = ConnectTvDto.builder().mode(resp.getData().getModel().toLowerCase())
						.publicIp(resp.getData().getPublicIp()).staIp(tvconnect.getStaIp())
						.staMac(tvconnect.getStaMac()).vlan(resp.getData().getVlan().getVlanName().replace("v","")).build();
				//			log.error("body{}"+body);
				if(resp.getData().getModel().equalsIgnoreCase("gpon")) {
					pageType = "remote";
				}else {
					pageType = "100";
				}

				String requestBody = "pagetype="+pageType+"&vlan="+resp.getData().getVlan().getVlanName().replace("v","") +"&staMac="+tvconnect.getStaMac();
				log.error(requestBody+"{req}");
				Mono<String> responseMono =  this.defaultWeclientBean.webClient.post().uri(PortalEndpointsConstant.WEB_PORTAL_AUTH)
						.contentType(MediaType.APPLICATION_FORM_URLENCODED) // Set content type to form-urlencoded
						.bodyValue(requestBody)
						.header("Authorization","Basic JDJhJDEwJExhQWg1eGhjbzpaMGhLSng1UnZ5bGVHNEhwdkQ3")
						.accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(String.class);

				try {
					String connectResponseJson = responseMono.block();
					if(connectResponseJson !=null) {
						return new Gson().fromJson(connectResponseJson,Map.class);
					}
				}catch(Exception ex) {
					ObjectNode node = JsonNodeFactory.instance.objectNode();
					node.put("success",false);
					node.put("message","Error occured while authenticating device");

					return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(node);
				}

			}



		}catch(Exception ex) {
			ex.printStackTrace();
			ObjectNode node = JsonNodeFactory.instance.objectNode();
			node.put("success",false);
			node.put("message","Error obtaining VLAN information");

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(node);
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

}
