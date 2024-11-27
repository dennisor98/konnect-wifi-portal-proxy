package net.sasakonnect.wifi_portal.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.SaveDeviceDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.UserDeviceService;

@CustomController
@RequestMapping("device")
@Tag(name="Device")
public class DeviceController {
	@Autowired
	UserDeviceService deviceService;
	
	@PostMapping()
	public Object addDevice(@Valid @RequestBody() SaveDeviceDto deviceDto) {
		return this.deviceService.saveUserDevice(deviceDto);
	}
	
	@PutMapping()
	public Object editDevice(@Valid @RequestBody() SaveDeviceDto deviceDto) {
		return this.deviceService.editUserDevice(deviceDto);
	}
	
	
	@DeleteMapping()
	public Object deleteDevice(@RequestParam(name="staMac") String staMac) {
		return this.deviceService.deleteUserDevice(staMac);
	}
	
	@GetMapping()
	public Object getUserDevices() {
		return this.deviceService.getUserDevices();
	}
	

	
	
}
