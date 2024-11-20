package net.sasakonnect.wifi_portal.services;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;

@Service
public class MessagingService {

    private final RabbitTemplate rabbitTemplate;
//    private final ConnectionFactory connection;
    private final Queue queue;
//    private final Channel channel;

    @Autowired
    public MessagingService(RabbitTemplate rabbitTemplate, Queue queue) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
//        this.connection = connection;
//        this.channel = channel;
    }

    public void sendMessage(String message) {
        // Send message to the queue
        rabbitTemplate.convertAndSend(queue.getName(),"sender", message);
        System.out.println("Sent message: " + message);
    }
    
//    public void receiveMessage() {
//    	Channel channel = connection;
////    	try {
////    		channel.queueDeclare(queue.getName(), false, false, false, null);
////    	}catch(Exception ex) {
////    		
////    	}
//    	 
//    }
}
