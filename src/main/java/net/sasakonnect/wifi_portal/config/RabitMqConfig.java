package net.sasakonnect.wifi_portal.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
public class RabitMqConfig {
		private static final String QUEUE_NAME = "merchant";
		private static final String CHANNEL_NAME = "merchant";


	    // Define the Connection Factory
	    @Bean
	     ConnectionFactory connectionFactory() {
	        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
	        connectionFactory.setUsername("guest"); // Default username
	        connectionFactory.setPassword("guest"); // Default password
	        return connectionFactory;
	    }

	    // Define a RabbitTemplate Bean
	    @Bean
	     RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
	        return new RabbitTemplate(connectionFactory);
	    }

	    // Define a Queue
	    @Bean
	     Queue queue() {
	        return new Queue(QUEUE_NAME, false); // Durable = false
	    }
	    
//	    @Bean
//	     Channel channel() {
//	        return Channel
//	    }
	
}
