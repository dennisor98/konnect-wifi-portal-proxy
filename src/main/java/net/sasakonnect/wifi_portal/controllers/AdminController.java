package net.sasakonnect.wifi_portal.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.CreateAppDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdatePackageDto;
import net.sasakonnect.wifi_portal.annotations.BackOfficeAuthFilter;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.domain.App;
import net.sasakonnect.wifi_portal.repository.AppRepository;
import net.sasakonnect.wifi_portal.services.AppService;
import net.sasakonnect.wifi_portal.services.PaymentService;
import net.sasakonnect.wifi_portal.services.PortalService;
import net.sasakonnect.wifi_portal.services.UserService;


@CustomController
@RequestMapping("admin")
@Tag(name="BackOffice")
@Slf4j
@BackOfficeAuthFilter
public class AdminController {
	@Autowired
	PortalService portalService;
	@Autowired
	PaymentService paymentService;
	@Autowired
	UserService userService;
	@Autowired
	AppService appService;
	
	@PutMapping("/package/update")
	public Object updatePackage(@Valid @RequestBody UpdatePackageDto pkg) {
		return this.portalService.updatePackage(pkg);
	}
	
	@PostMapping("getSubscriptionpackages")
	public Object getInternetPackages() {
		return this.portalService.getInternetPackages();
	}
	
	@GetMapping("payments")
	public Object getPayments(
			@RequestParam(name="pageNumber",defaultValue="0") Integer pageNumber,
			@RequestParam(name="pageSize",defaultValue="20") Integer pageSize
			) {
	
		if(pageSize > 100) {
			pageNumber = 100;
		}
		return this.paymentService.getPayments(PageRequest.of(pageNumber,pageSize,Sort.Direction.DESC,"updatedAt"));
	}
	
	@GetMapping("/apps")
	public Object getApps() {
         return this.appService.getApps();
	}
	
	@PostMapping("/app")
	public Object createApp(@Valid @RequestBody CreateAppDto payload) {
		return this.appService.createApp(payload);
	}

	@GetMapping("/users")
	public Object getUsers(
			@RequestParam(name="pageNumber",defaultValue="0") Integer pageNumber,
			@RequestParam(name="pageSize",defaultValue="20") Integer pageSize
			) {
		return this.userService.getUsers(PageRequest.of(pageNumber,pageSize));
	}
	
}
