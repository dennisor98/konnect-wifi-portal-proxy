package net.sasakonnect.wifi_portal.ResponseDto;

import lombok.Data;
import java.util.List;

@Data
public class StkCallbackResponseDTO {

    private Body Body;

    @Data
    public static class Body {
        private StkCallback stkCallback;
    }

    @Data
    public static class StkCallback {
        private String MerchantRequestID;
        private String CheckoutRequestID;
        private int ResultCode;
        private String ResultDesc;
        private CallbackMetadata CallbackMetadata;
    }

    @Data
    public static class CallbackMetadata {
        private List<Item> Item;
    }

    @Data
    public static class Item {
        private String Name;
        private Object Value; 
    }
}

