package net.sasakonnect.wifi_portal.RequestDto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sasakonnect.wifi_portal.domain.App;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantTransactionNotificationDto implements Serializable {
private static final long serialVersionUID = 3874063797996847059L;
//   private static final long serialVersionUID = -2627052303916262156L;
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
   String deviceMac;
   String platform;
   App app;
}
