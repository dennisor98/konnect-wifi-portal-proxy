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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.RequestDto.AssignRoleDto;
import net.sasakonnect.wifi_portal.RequestDto.CreateAppDto;
import net.sasakonnect.wifi_portal.RequestDto.DelRoleDto;
import net.sasakonnect.wifi_portal.RequestDto.PayMethodDto;
import net.sasakonnect.wifi_portal.RequestDto.RoleDto;
import net.sasakonnect.wifi_portal.RequestDto.RoleEditDto;
import net.sasakonnect.wifi_portal.RequestDto.RolePermDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdateAppDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdatePackageDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdatePasswordDto;
import net.sasakonnect.wifi_portal.RequestDto.UpdatePayMethodDto;
import net.sasakonnect.wifi_portal.annotations.BackOfficeAuthFilter;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.annotations.HasPermission;
import net.sasakonnect.wifi_portal.constants.GlobalPermissionsConstants;
import net.sasakonnect.wifi_portal.domain.App;
import net.sasakonnect.wifi_portal.repository.AppRepository;
import net.sasakonnect.wifi_portal.services.AppService;
import net.sasakonnect.wifi_portal.services.PaymentService;
import net.sasakonnect.wifi_portal.services.PortalService;
import net.sasakonnect.wifi_portal.services.RoleService;
import net.sasakonnect.wifi_portal.services.UserService;


@CustomController
@RequestMapping("admin")
@Tag(name="BackOffice")
@Slf4j
@BackOfficeAuthFilter()
public class AdminController {
	@Autowired
	PortalService portalService;
	@Autowired
	PaymentService paymentService;
	@Autowired
	UserService userService;
	@Autowired
	AppService appService;
	@Autowired
	RoleService roleService;
	
	@HasPermission(GlobalPermissionsConstants.CanGetSubscriptions.PERMISSION)
	@PutMapping("/package/update")
	public Object updatePackage(@Valid @RequestBody UpdatePackageDto pkg) {
		return this.portalService.updatePackage(pkg);
	}
	
	@PostMapping("getSubscriptionpackages")
	public Object getInternetPackages() {
		return this.portalService.getInternetPackages();
	}
	
    @HasPermission(GlobalPermissionsConstants.CanGetTransactions.PERMISSION)
	@GetMapping("payments")
	public Object getPayments(
			@RequestParam(name="pageNumber",defaultValue="0") Integer pageNumber,
			@RequestParam(name="pageSize",defaultValue="20") Integer pageSize,
			@RequestParam(name="filter",defaultValue="all") String filter
			) {
	
		if(pageSize > 100) {
			pageSize = 100;
		}
		return this.paymentService.getPayments(PageRequest.of(pageNumber,pageSize,Sort.Direction.DESC,"updatedAt"),filter);
	}
	
    
    @HasPermission(GlobalPermissionsConstants.CanGetTransactions.PERMISSION)
	@GetMapping("payment/search")
	public Object searchPayment(
			@RequestParam(name="searchTerm") String searchTerm,
			@RequestParam(name="pageNumber",defaultValue="0") Integer pageNumber,
			@RequestParam(name="pageSize",defaultValue="20") Integer pageSize
			) {
	
		if(pageSize > 10) {
			pageSize = 10;
		}
		return this.paymentService.searchPayment(searchTerm,PageRequest.of(pageNumber,pageSize,Sort.Direction.DESC,"updatedAt"));
	}
	
	
	@PostMapping("/app")
	public Object createApp(@Valid @RequestBody CreateAppDto payload) {
		return this.appService.createApp(payload);
	}
	
	@GetMapping("/apps")
	public Object getApps() {
         return this.appService.getApps();
	} 
	
	@PutMapping("/app")
	public Object updateApp(@Valid @RequestBody UpdateAppDto payload) {
		return this.appService.updateApp(payload);
	}
	
	@DeleteMapping("/app")
	public Object deleteApp(@RequestParam("appId") String appId) {
		return this.appService.deleteApp(appId);
		
	}

	
	@HasPermission(GlobalPermissionsConstants.CanGetUsers.PERMISSION)
	@GetMapping("/users")
	public Object getUsers(
			@RequestParam(name="pageNumber",defaultValue="0") Integer pageNumber,
			@RequestParam(name="pageSize",defaultValue="20") Integer pageSize
			) {
		return this.userService.getUsers(PageRequest.of(pageNumber,pageSize));
	}
	
	
	@HasPermission(GlobalPermissionsConstants.CanGetSubscriptions.PERMISSION)
	@GetMapping("/user/subscriptions")
	public Object getUserSubsById(@RequestParam("phone") String phone) {
		return this.portalService.getUserSubscriptionsByPhone(phone);
	}
	
	@HasPermission(GlobalPermissionsConstants.CanGetRoles.PERMISSION)
	@GetMapping("/roles")
	public Object getRoles() {
		return this.roleService.getAllRoles();
	}
	
	
	@HasPermission(GlobalPermissionsConstants.CanAddUser.PERMISSION)
	@PostMapping("/user/role")
	public Object createUser(@Valid @RequestBody AssignRoleDto roleDto) {
		return this.userService.addAdminUser(roleDto);
	}
	
	@HasPermission(GlobalPermissionsConstants.CanAssignRole.PERMISSION)
	@GetMapping("/user/role")
	public Object getUserRoleById(@RequestParam("userId") String userId) {
		return this.roleService.getUserRoleByUserId(userId);
	}
	
	@HasPermission(GlobalPermissionsConstants.CanGetUsers.PERMISSION)
	@GetMapping("/user/downloadTrend")
	public Object getAppDownloadTrends() {
		return this.userService.getAppDownloadsTrend();
	}
	
	
	@HasPermission(GlobalPermissionsConstants.CanGetUsers.PERMISSION)
	@GetMapping("/user/searchByPhone")
	public Object searchUserByPhone(
			@RequestParam("phone") String phone,
			@RequestParam(name="pageNumber",defaultValue="0") Integer pageNumber,
			@RequestParam(name="pageSize",defaultValue="10") Integer pageSize) {
		if(pageSize > 10) {
			pageSize =  10;
		}
		var page =  PageRequest.of(pageNumber,pageSize);
		return this.userService.searchUserByPhone(phone,page);
	}
	
	
	
	
	@HasPermission(GlobalPermissionsConstants.CanAssignRole.PERMISSION)
	@GetMapping("/permissions")
	public Object getPermissions() {
		
		return this.roleService.getAllPermissions();
	}
	

	  @PostMapping("/role")
	  @HasPermission(GlobalPermissionsConstants.CreateRole.PERMISSION)
	  public Object createRole(@Valid @RequestBody() RoleDto roleDto) {
		  return this.roleService.createRole(roleDto);
	  }
	  
	  @DeleteMapping("/role")
	  @HasPermission(GlobalPermissionsConstants.DeleteRole.PERMISSION)
	  public Object deleteRole(@Valid @RequestBody() DelRoleDto roleDto) {
		  return this.roleService.deleteRole(roleDto);
	  }
	  
	  @PutMapping("/role")
	  @HasPermission(GlobalPermissionsConstants.CanEditRole.PERMISSION)
	  public Object editRole(@Valid @RequestBody() RoleEditDto roleDto) {
		  return this.roleService.editRole(roleDto);
	  }
	  
	  
	  @PostMapping("/role/permissions")
	  @HasPermission(GlobalPermissionsConstants.CanGetRoles.PERMISSION)
	  public Object assignPermissionsToRole(@Valid @RequestBody() RolePermDto roleDto) {
		  return this.roleService.assignPermissionsToRole(roleDto);
	  }
	  
	  
	  
	  @GetMapping("/role/permissions")
	  @HasPermission(GlobalPermissionsConstants.CreateRole.PERMISSION)
	  public Object getRolePermissions(@RequestParam(name="roleId",required=true) String roleId) {
		  return this.roleService.getRolePermissions(roleId);
	  }
	  
	  
	  @GetMapping("/stats/summary")
	  @HasPermission(GlobalPermissionsConstants.CanGetTransactions.PERMISSION)
	  public Object getStatSummary() {
		  return this.portalService.getStatSummary();
	  }
	  
	  
	  @GetMapping("/payment/summary")
	  @HasPermission(GlobalPermissionsConstants.CanGetTransactions.PERMISSION)
	  public Object getPaymentStatSummary() {
		  return this.portalService.getTransactionsTrend();
	  }
	  
	  
	  @PostMapping("/payment/option")
	  @HasPermission(GlobalPermissionsConstants.CanManagePayment.PERMISSION)
	  public Object createPayMethod(@Valid @RequestBody()  PayMethodDto payDto) {
		return this.paymentService.createPaymentMethod(payDto);  
	  }
	  
	  
	  @PutMapping("/payment/option")
	  @HasPermission(GlobalPermissionsConstants.CanManagePayment.PERMISSION)
	  public Object updatePayMethod(@Valid @RequestBody() UpdatePayMethodDto payDto) {
		return this.paymentService.updatePayMethod(payDto);  
	  }
	  
	  
	  @DeleteMapping("/payment/option")
	  @HasPermission(GlobalPermissionsConstants.CanManagePayment.PERMISSION)
	  public Object deletePayMethod(@RequestParam("payId") String payId) {
		  return this.deletePayMethod(payId);
	  }
	  
	  @PutMapping("/user/password/update")
	  public Object updateAdminUserPassword(@Valid @RequestBody() UpdatePasswordDto pwDto) {
		  return this.userService.updateAdminPassword(pwDto);
	  }
	  
	  
	  
	  
	  
	
	
	
}
