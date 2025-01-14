package net.sasakonnect.wifi_portal.RequestDto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PollTxStatusDto {
   @NotNull()
   String txId;
}
