package net.sasakonnect.wifi_portal.RequestDto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BulkVirtualSubDto {
   @NotNull
   String amount;
   
   @NotNull
   List<String> contacts;
   
   
}
