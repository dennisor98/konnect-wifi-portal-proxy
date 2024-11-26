package net.sasakonnect.wifi_portal.services;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.rabbitmq.client.ConnectionFactory;

@Service
public class MessagingService {

//    private final RabbitTemplate rabbitTemplate;
//    private final ConnectionFactory connection;
//    private final Channel channel;

//    public MessagingService(RabbitTemplate rabbitTemplate,ConnectionFactory connection) {
//        this.rabbitTemplate = rabbitTemplate;
//        this.connection = connection;
////        this.channel = channel;
//    }

    public void sendMessage(String message) {
        // Send message to the queue
//        rabbitTemplate.convertAndSend("q","sender", message);
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
