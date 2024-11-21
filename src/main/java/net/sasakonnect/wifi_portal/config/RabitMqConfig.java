//package net.sasakonnect.wifi_portal.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.stereotype.Component;
//
//import com.rabbitmq.client.ConnectionFactory;
//
//@Component
//@Configuration
//public class RabitMqConfig {
//		private static final String QUEUE_NAME = "merchant";
//		private static final String CHANNEL_NAME = "merchant";
//
//
//	    // Define the Connection Factory
//	    @Bean
//	     ConnectionFactory connectionFactory() {
//	    	ConnectionFactory connectionFactory = new ConnectionFactory();
//	    	connectionFactory.setHost("localhost");
//	    	connectionFactory.setPort(5672);
//	        connectionFactory.setUsername("ahdev"); 
//	        connectionFactory.setPassword("qwer"); 
//	        return connectionFactory;
//	    }
//
//	    
////	    @Bean
////	     Channel channel() {
////	        return Channel
////	    }
//	
//}
