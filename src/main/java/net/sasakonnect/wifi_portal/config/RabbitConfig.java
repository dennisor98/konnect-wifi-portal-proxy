package net.sasakonnect.wifi_portal.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

//    @Bean
//    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter converter) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(converter);
//        return rabbitTemplate;
//    }

    @Bean(name="paymentRequestQueue")
    Queue paymentRequestQueue() {
        return new Queue("paymentRequestQueue", true);
    }

    @Bean(name="checkOutIdConfirmationQueue")
    Queue checkOutIdConfirmationQueue() {
        return new Queue("checkOutIdConfirmationQueue", true);
    }

    @Bean
    Queue transactionCallBackNotificationQueue() {
        return new Queue("transactionCallBackNotificationQueue", true);
    }

    @Bean(name="failedPaymentNotificationQueue")
    Queue failedPaymentNotificationQueue() {
        return new Queue("failedPaymentNotificationQueue", true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("transactionExchange");
    }

    @Bean
    Binding bindingCheckOutIdConfirmation(
    		@Qualifier("checkOutIdConfirmationQueue") Queue checkOutIdConfirmationQueue,
            TopicExchange exchange) {
        return BindingBuilder.bind(checkOutIdConfirmationQueue).to(exchange).with("transaction.confirm");
    }

    @Bean
    Binding bindingPaymentRequest(
    		@Qualifier("paymentRequestQueue")    Queue paymentRequestQueue,
            TopicExchange exchange) {
        return BindingBuilder.bind(paymentRequestQueue).to(exchange).with("transaction.payment");
    }
    
    @Bean
    Binding bindingPaymentNotification(
    		@Qualifier("transactionCallBackNotificationQueue")  Queue transactionCallBackNotificationQueue,
            TopicExchange exchange) {
        return BindingBuilder.bind(transactionCallBackNotificationQueue).to(exchange).with("transaction.notify");
    }

    @Bean
    Binding bindingFailedPaymentNotification(
    		@Qualifier("failedPaymentNotificationQueue")   Queue failedPaymentNotificationQueue,
            TopicExchange exchange) {
        return BindingBuilder.bind(failedPaymentNotificationQueue).to(exchange).with("transaction.failedPayment");
    }
}
