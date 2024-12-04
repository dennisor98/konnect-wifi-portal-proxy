package net.sasakonnect.wifi_portal.RequestDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MpesaPaymentValidationDto {
  @JsonProperty("TransactionType")
  String TransactionType;
  @JsonProperty("TransID")
  String TransID;
  @JsonProperty("TransTime")
  String TransTime;
  @JsonProperty("TransAmount")
  String TransAmount;
  @JsonProperty("BusinessShortCode")
  String BusinessShortCode;
  @JsonProperty("BillRefNumber")
  String BillRefNumber;
  @JsonProperty("InvoiceNumber")
  String InvoiceNumber;
  @JsonProperty("OrgAccountBalance")
  String OrgAccountBalance;
  @JsonProperty("ThirdPartyTransID")
  String ThirdPartyTransID;
  @JsonProperty("MSISDN")
  String MSISDN;
  @JsonProperty("FirstName")
  String FirstName;
  
}
