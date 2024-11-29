package net.sasakonnect.wifi_portal.RequestDto;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MpesaCallBackDto {
	@JsonProperty("Body")
	private Body Body;

	
	@Data
	public static class Body {
		@JsonProperty("stkCallback")
		private StkCallback stkCallback;
	}

	@Data
	public static class StkCallback {
		@JsonProperty("MerchantRequestID")
		private String MerchantRequestID;

		@JsonProperty("CheckoutRequestID")
		private String CheckoutRequestID;

		@JsonProperty("ResultCode")
		private int ResultCode;

		@JsonProperty("ResultDesc")
		private String ResultDesc;

		@JsonProperty("CallbackMetadata")
		private CallbackMetadata CallbackMetadata;

	
	}

	@Data
	public static class CallbackMetadata {
		@JsonProperty("Item")
		private List<Item> Item;
	}

	
	@Data
	public static class Item {
		@JsonProperty("Name")
		private String Name;

		@JsonProperty("Value")
		private Object Value; 
	}
}
