package net.sasakonnect.wifi_portal.config;



import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
	  @Bean
	     Jackson2JsonMessageConverter jsonMessageConverter() {
	        return new Jackson2JsonMessageConverter();
	    }
    @Bean
     Queue paymentRequestQueue() {
        return new Queue("paymentRequestQueue", true);
    }
    @Bean
    Queue checkOutIdConfirmationQueue() {
       return new Queue("checkOutIdConfirmationQueue", true);
   }
    @Bean
     Queue failedPaymentNotificationQueue() {
        return new Queue("failedPaymentNotificationQueue", true);
    }

    @Bean
     TopicExchange exchange() {
        return new TopicExchange("transactionExchange");
    }

 // Bind each queue with a different routing key pattern
    @Bean
     Binding bindingCheckOutIdConfirmation(Queue checkOutIdConfirmationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(checkOutIdConfirmationQueue).to(exchange).with("transaction.confirm");
    }
    @Bean
    Binding bindingPaymentRequest(Queue paymentRequestQueue, TopicExchange exchange) {
       return BindingBuilder.bind(paymentRequestQueue).to(exchange).with("transaction.payment");
   }

    @Bean
     Binding bindingfailedPaymentNotification(Queue failedPaymentNotificationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(failedPaymentNotificationQueue).to(exchange).with("transaction.failedPayment");
    }

    
}
