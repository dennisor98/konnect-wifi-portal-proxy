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

    @Bean
    Queue failedPaymentNotificationQueue0() {
        return new Queue("failedPaymentNotificationQueue0", true);
    }
    
    @Bean
    Queue failedPaymentNotificationQueue1() {
        return new Queue("failedPaymentNotificationQueue1", true);
    }
    
    @Bean
    Queue failedPaymentNotificationQueue2() {
        return new Queue("failedPaymentNotificationQueue2", true);
    }
    
    
    @Bean(name="transactionStatusQueue")
    Queue transactionStatusNotificationQueue() {
        return new Queue("transactionStatusQueue", true);
    }
    
    @Bean(name="transactionConfirmedNotificationQueue")
    Queue transactionConfirmedNotificationQueue() {
        return new Queue("transactionConfirmedNotificationQueue", true);
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
    Binding bindingTransactionConfirmedNotificationQueue(
    		@Qualifier("transactionConfirmedNotificationQueue") Queue transactionConfirmedNotificationQueue,
            TopicExchange exchange) {
        return BindingBuilder.bind(transactionConfirmedNotificationQueue).to(exchange).with("transaction.merchantNotify");
    }
    
    @Bean
    Binding bindingtransactionStatusRequest(
    		@Qualifier("transactionStatusQueue") Queue transactionStatusQueue,
            TopicExchange exchange) {
        return BindingBuilder.bind(transactionStatusQueue).to(exchange).with("transaction.status");
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
    Binding bindingFailedPaymentNotification0(
    		@Qualifier("failedPaymentNotificationQueue0")   Queue failedPaymentNotificationQueue0,
            TopicExchange exchange) {
        return BindingBuilder.bind(failedPaymentNotificationQueue0).to(exchange).with("transaction.failedPayment0");
    }
    
    @Bean
    Binding bindingFailedPaymentNotification1(
    		@Qualifier("failedPaymentNotificationQueue1")   Queue failedPaymentNotificationQueue1,
            TopicExchange exchange) {
        return BindingBuilder.bind(failedPaymentNotificationQueue1).to(exchange).with("transaction.failedPayment1");
    }
    
    @Bean
    Binding bindingFailedPaymentNotification2(
    		@Qualifier("failedPaymentNotificationQueue2")   Queue failedPaymentNotificationQueue2,
            TopicExchange exchange) {
        return BindingBuilder.bind(failedPaymentNotificationQueue2).to(exchange).with("transaction.failedPayment2");
    }
    
    
}
