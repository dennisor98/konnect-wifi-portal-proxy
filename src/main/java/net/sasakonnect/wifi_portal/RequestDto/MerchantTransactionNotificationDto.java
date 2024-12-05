package net.sasakonnect.wifi_portal.RequestDto;

import lombok.Builder;
import lombok.Data;
import net.sasakonnect.wifi_portal.domain.App;

@Data
@Builder
public class MerchantTransactionNotificationDto {
   String transType;
   String transId;
   String transTime;
   String transAmount;
   String businessShortCode;
   String billRefNumber;
   String mobile;
   String name;
   String userId;
   String konnectTransId;
   App app;
}
