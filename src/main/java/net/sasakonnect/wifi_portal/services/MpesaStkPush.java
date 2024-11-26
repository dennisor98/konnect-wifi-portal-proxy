package net.sasakonnect.wifi_portal.services;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MpesaStkPush {

    // Hardcoded keys
    private static final String CONSUMER_KEY = "cWYIi6VMkuqOyrVaRnnAJZ4Ywwq7kFuCbm1Act7QR6SsH0jA";
    private static final String CONSUMER_SECRET = "uHESOeGt8rdZZZX0uAeJqOrBxrGuXFK1yiRGmzrYsjf1B3jNrAdQOPbQAIbP5Sqa";
    private static final String BUSINESS_SHORTCODE = "174379";
    private static final String PASSWORD = "MTc0Mzc5YmZiMjc5ZjlhYTliZGJjZjE1OGU5N2RkNzFhNDY3Y2QyZTBjODkzMDU5YjEwZjc4ZTZiNzJhZGExZWQyYzkxOTIwMjQxMTI1MTU1NzE5";
    private static final String LIPA_NA_MPESA_URL = "https://api.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
    private static final String OAUTH_URL = "https://api.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";

    // RestTemplate bean injected by Spring
    private final RestTemplate restTemplate;

    public MpesaStkPush(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Step 1: Get Access Token
    public String getAccessToken() {
        // Prepare Basic Auth for Authorization header
        String credentials = CONSUMER_KEY + ":" + CONSUMER_SECRET;
        String base64Credentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + base64Credentials);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Send request to get access token
        ResponseEntity<String> response = restTemplate.exchange(OAUTH_URL, HttpMethod.GET, entity, String.class);
             System.err.print(response.getBody());
        if (response.getStatusCode() == HttpStatus.OK) {
            String jsonResponse = response.getBody();
            // Parse the access token from the response JSON
            return jsonResponse.split("\"access_token\":\"")[1].split("\"")[0];
        } else {
            System.out.println("Failed to retrieve access token. Status: " + response.getStatusCode());
            return null;
        }
    }

    // Step 2: Initiate STK Push Request
    public String initiateSTKPush(String accessToken) {
        // Prepare the request body JSON
        String jsonPayload = "{\n" +
                "    \"BusinessShortCode\": " + BUSINESS_SHORTCODE + ",\n" + 
                "    \"Password\": \"" + PASSWORD + "\",\n" + 
                "    \"Timestamp\": \"20241125155719\",\n" + // Timestamp should be dynamically generated
                "    \"TransactionType\": \"CustomerPayBillOnline\",\n" +
                "    \"Amount\": 1,\n" +
                "    \"PartyA\": 254703454954,\n" + // PartyA (payer's phone number)
                "    \"PartyB\": " + BUSINESS_SHORTCODE + ",\n" + // PartyB (shortcode)
                "    \"PhoneNumber\": 254703454954,\n" + // PartyA's phone number
                "    \"CallBackURL\": \"https://mydomain.com/path\",\n" + // Callback URL
                "    \"AccountReference\": \"CompanyXLTD\",\n" + // AccountReference
                "    \"TransactionDesc\": \"Payment of X\"\n" + // TransactionDesc
                "}";

        // Set the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Prepare the HttpEntity with the body and headers
        HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

        // Make the request to STK Push
        ResponseEntity<String> response = restTemplate.exchange(LIPA_NA_MPESA_URL, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody(); // Return the response body (STK Push response)
        } else {
            return "Error: " + response.getStatusCode() + " - " + response.getBody();
        }
    }
}
