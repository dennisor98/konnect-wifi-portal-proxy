package net.sasakonnect.wifi_portal.RequestDto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class MpesaResultDto {
    @JsonProperty()
	private ResultDto Result;

	@Data
	public static class ResultDto {
		private String ConversationID;
		private String OriginatorConversationID;
		private ReferenceDataDto ReferenceData;
		private int ResultCode;
		private String ResultDesc;
		private ResultParametersDto ResultParameters;
		private int ResultType;
		private String TransactionID;


	}

	@Data
	public static class ReferenceDataDto {
		private ReferenceItemDto ReferenceItem;

	}

	@Data
	public static class ReferenceItemDto {
		private String Key;
	}

	@Data
	public static class ResultParametersDto {
		private List<ResultParameterDto> ResultParameter;

	}

	@Data
	public static class ResultParameterDto {
		private String Key;
		private String Value;

	}
}
