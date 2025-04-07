package net.sasakonnect.wifi_portal.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import net.sasakonnect.wifi_portal.RequestDto.BulkVirtualSubDto;
import net.sasakonnect.wifi_portal.RequestDto.VirtualSubDto;
import net.sasakonnect.wifi_portal.annotations.CustomController;
import net.sasakonnect.wifi_portal.services.PnalService;

@CustomController
@RequestMapping("pnal")
@Tag(name="PNAL")
public class PnalController {
	@Autowired
	PnalService pnalService;
	
   @PostMapping("sub")
   public Object createVirtualSub(@Valid @RequestBody VirtualSubDto vsub) {
	   return this.pnalService.createVirtualSubScription(vsub);
   }
   
   @PostMapping("sub/bulk/v1")
   public Object createBulkVirtualSub(@Valid @RequestBody List<VirtualSubDto> vsub) {
	   return this.pnalService.createBulkVirtualSubs(vsub);
   }
   
   @PostMapping("sub/bulk/v2")
   public Object createBulkVirtualSubV2(@Valid @RequestBody BulkVirtualSubDto vsub) {
	   return this.pnalService.createBulkVirtualSubsV2(vsub);
   }
   
}
