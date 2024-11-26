package net.sasakonnect.wifi_portal.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        rabbitTemplate.convertAndSend("transactionExchange", "transaction.payment", paymentRequest); 
        return payment_checkoutId;
    }
    
    public void sendMpesaCheckoutRequestId(PaymentRequest paymentRequest) {
        rabbitTemplate.convertAndSend("transactionExchange", "transaction.confirm", paymentRequest);
        return ;
    }

	public void updatePayment(String requestBody) {
        rabbitTemplate.convertAndSend("transactionExchange", "transaction.callBack", requestBody);

		// TODO Auto-generated method stub
		
	}
}
