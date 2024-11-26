package net.sasakonnect.wifi_portal.services;

import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.rabbitmq.client.*;

import net.sasakonnect.wifi_portal.RequestDto.PollMpesaDto;

@Service
public class MessagingService {

   private final ConnectionFactory connectionFactory;

   
   @Autowired
    public MessagingService(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
   
   
   @Autowired
   RabbitTemplate rabbitTemplate;
   

    public Object processMpesaStkPush(PollMpesaDto message) {
    	 try (Channel channel = createUniqueChannel()) {
             String queueName = "MPESA_STK";
             channel.queueDeclare(queueName, true, false, false, null);
             // Publish a message
             ObjectMapper objectMapper = new ObjectMapper();
             byte[] messageBytes = objectMapper.writeValueAsBytes(message);
             
             // Publish the serialized message to the queue
             channel.basicPublish("", queueName, 
            		    MessageProperties.PERSISTENT_TEXT_PLAIN, // Change content type to JSON
            		    messageBytes);// Publish the message as byte[]
//             rabbitTemplate.convertAndSend(new Gson().toJson(message));

             System.out.println("Message sent: " + message);
             
             ObjectNode res =  JsonNodeFactory.instance.objectNode();
             res.put("success",true);
             res.put("message", "Job created");
             
             return res;
         } catch (Exception e) {
             e.printStackTrace();
         }
    	 
    	 return null;
    }
    
    
    public Channel createUniqueChannel() {
    	try {
    		Connection connection = connectionFactory.createConnection();
    		Channel channel = connection.createChannel(false);

    		System.out.println("Unique channel created: " + channel);

    		return channel;

    	} catch (Exception e) {
    		throw new RuntimeException("Failed to create channel", e);
    	}
    } 
    
    @RabbitListener(queues = "MPESA_STK")
    public void receiveMessage(String poll) {
    	 ObjectMapper objectMapper = new ObjectMapper();
    	    try {
    	        // Convert JSON string to a Map
    	        Map<String, Object> map = objectMapper.readValue(poll, new TypeReference<Map<String, Object>>() {});
    	        
    	        // Access specific fields
    	        System.out.println("Received checkoutRequestID: " + map.get("checkoutRequestID"));
    	        System.out.println("Full Map: " + map);
    	    } catch (Exception e) {
    	        e.printStackTrace();
    	        // Handle deserialization error
    	    }
//            System.out.println("Received message: " + poll);

    }
    
    public void processMpesaStkQueue(String checkoutRequestId) {
    	
    }
}
