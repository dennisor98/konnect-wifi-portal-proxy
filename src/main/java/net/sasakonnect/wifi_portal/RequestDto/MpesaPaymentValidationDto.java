package net.sasakonnect.wifi_portal.RequestDto;

import lombok.Data;

@Data
public class MpesaPaymentValidationDto {
  String TransactionType;
  String TransID;
  String TransTime;
  String TransAmount;
  String BusinessShortCode;
  String BillRefNumber;
  String InvoiceNumber;
  String OrgAccountBalance;
  String ThirdPartyTransID;
  String MSISDN;
  String FirstName;
  
}
