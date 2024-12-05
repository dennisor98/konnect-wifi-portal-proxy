package net.sasakonnect.wifi_portal.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sasakonnect.wifi_portal.RequestDto.MpesaPaymentValidationDto;
import net.sasakonnect.wifi_portal.RequestDto.sdk.PaymentRequest;
import net.sasakonnect.wifi_portal.beans.AdvancedUniqueKeyGenerator;

@Service
public class RabbitMqSenderService {

    private final RabbitTemplate rabbitTemplate;
    

    public RabbitMqSenderService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Object sendPaymentRequest(PaymentRequest paymentRequest) {
        var payment_checkoutId =AdvancedUniqueKeyGenerator.generateUniqueKey().toUpperCase();
        paymentRequest.setKonnectCheckoutID(payment_checkoutId);
    	System.out.println(paymentRequest);
    	ObjectMapper objectMapper = new ObjectMapper();
    	try {
    	    String json = objectMapper.writeValueAsString(paymentRequest);
            rabbitTemplate.convertAndSend("transactionExchange", "transaction.payment", json); 

    	    System.out.println("Payload: " + json);
    	} catch (JsonProcessingException e) {
    	    e.printStackTrace();
    	}
        return payment_checkoutId;
    }
    
    public void requestPaymentStatus(MpesaPaymentValidationDto data) {
    	this.rabbitTemplate.convertAndSend("transactionExchange","transaction.status",data);
    }
    
    public void sendMpesaCheckoutRequestId(PaymentRequest paymentRequest) {
        rabbitTemplate.convertAndSend("transactionExchange", "transaction.confirm", paymentRequest);
        return ;
    }

	public void updatePayment(String requestBody) {
//        rabbitTemplate.convertAndSend("transactionExchange", "transaction.callBack", requestBody);

		// TODO Auto-generated method stub
		
	}
}
