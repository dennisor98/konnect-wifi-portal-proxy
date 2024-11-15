package net.sasakonnect.wifi_portal.constants;

import org.springframework.context.annotation.Configuration;

@Configuration
public class PortalEndpointsConstant {
	
	public final  static  String BASE_URL = "https://api.sasakonnect.net";
	public final  static  String PORTAL_AUTH = BASE_URL+"/webPortalAuthAttempt";
	public final  static  String CHAT_SERVER = "http://105.29.165.232:23045/auth/api/v1/user-register";

	public final  static  String RE_SEND_OTP = BASE_URL+"/sendOTP";
	public final  static  String SEND_OTP = BASE_URL+"/login";

	public final  static  String VALIDATE_OTP = BASE_URL+"/confirmOTP";
	public final  static  String GET_PACKAGES = BASE_URL+"/getSubscriptionpackages";
	public final  static  String GET_PACKAGE_BY_ID = BASE_URL+"/getSubscriptionpackageById";
	public final  static  String BUY_PACKAGE_THOUGH_MPESA = BASE_URL+"/makeSubscriptionPayment";
	public final  static  String POLL_MPESA_FOR_PAYMENT_UPDATE =BASE_URL+"/getMpesaPaymentByCheckoutRequestID";
	public final  static  String CONFIRM_PACKAGE_THOUGH_MPESA_TILL = BASE_URL+"/makeTillPayment";
	public final  static  String ACTIVE_SUBSCRIPTION_BY_USER_ID = BASE_URL+"/getActiveSubscriptionrenewalsByUserId";
	public final  static  String ADD_DEVICE_TO_PACKAGE = BASE_URL+"/validateExtraDevice";
	public final  static  String UPDATE_CUSTOMER_INFO = BASE_URL+"/updateCustomerInfo";
	public final  static  String GET_ACTIVE_DEVICE = BASE_URL+"/getDevices";
	public final  static  String SWAP_DEVICE = BASE_URL+"/changeDevice";
	public final  static  String CREATE_ACCOUNT = BASE_URL+"/register";
	public final  static  String TRANSACTIONS = BASE_URL+"/get_client_subs";
	public final  static  String TRANSACTIONS_BY_ID = BASE_URL+"/getSubsByUserId";
	public final  static  String GET_USER_TOKEN = BASE_URL+"/getUserToken";
	public final  static  String GET_KOMP_AUTH_TOKEN = "https://gw.sasakonnect.net/komp/api/v2/create-token";
	public final  static  String GET_VLAN_INFO = "https://gw.sasakonnect.net/komp/api/v2/setups";


	// https://api.sasakonnect.net/getUserToken
	//lark endpoints
	public final  static  String LARK_HOST = "https://open.larksuite.com";
	public final  static  String TENANT_ACCESS_TOKEN = BASE_URL+"/open-apis/auth/v3/app_access_token/internal";
	public final  static  String SEND_MESSAGE = BASE_URL+"/open-apis/message/v4/send/";
	public final  static  String LIST_GROUPS = BASE_URL+"/open-apis/im/v1/chats";
	public final  static  String GET_GROUP = BASE_URL+"/open-apis/im/v1/chats/:chat_id";
	public final  static  String SEND_REPLY_MESSAGE = BASE_URL+"/open-apis/im/v1/messages/:message_id/reply";
	public final  static  String GET_MESSAGE = BASE_URL+"/open-apis/im/v1/messages/:message_id";
	public final  static  String LARK_IMAGES = BASE_URL+"/open-apis/im/v1/images";
	public final  static  String LARK_RESOURCES = BASE_URL+"/open-apis/im/v1/messages";
	//https://open.larksuite.com/open-apis/im/v1/messages/om_d1a52ef8fa3c67b203581868d114077a/resources/img_v2_5b8eddfd-4469-4324-8efd-02f85122dd6h?type=image

	//Konnect endpoints
	public final  static  String KOMP_HOST = "https://gw.sasakonnect.net/komp/api";
	public final  static  String GET_PAP = BASE_URL+"/client/information";
	public final  static  String GET_CLIENT_AP = BASE_URL+"/komp_assistant/clients_data.php";
	//http://app.sasakonnect.net:13000/api/clients_data.php?mac=F8-8C-21-56-0E-1D&output_format=JSON_PRETTY_PRINT
	/**
	 * http://app.sasakonnect.net:13000/api/clients.php?mac=34-F7-16-F5-2C-22&user=ahadi&key=$2y$10$HR9jsyCiYIK5z5/abdh4seXM/lwX7X4KkxAxDIIxPCksZ0CJ9RujW
	 */
	public final  static  String WHATSAPP_HOST = "https://graph.facebook.com/v16.0";
	public final  static  String REPLY_WHATSAPP= BASE_URL+"/108562478572747/messages";
	public final  static  String  MEDIAURL = "https://mobile.sasakonnect.net/v1/files";
}
