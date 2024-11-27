//package net.sasakonnect.wifi_portal.services;
//
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.fasterxml.jackson.databind.node.JsonNodeFactory;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;
//
//@Service
//public class MessagingService {
//
//   private final ConnectionFactory connectionFactory;
//
//   
//   @Autowired
//    public MessagingService(ConnectionFactory connectionFactory) {
//        this.connectionFactory = connectionFactory;
//    }
//
//    public Object processMpesaStkPush(String message) {
//    	 try (Channel channel = createUniqueChannel()) {
//             String queueName = "MPESA_STK";
//             channel.queueDeclare(queueName, true, false, false, null);
//             // Publish a message
//             channel.basicPublish("", queueName, null, message.getBytes());
//             System.out.println("Message sent: " + message);
//             
//             ObjectNode res =  JsonNodeFactory.instance.objectNode();
//             res.put("success",true);
//             res.put("message", "Job created");
//             
//             return res;
//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//    	 
//    	 return null;
//    }
//    
//    
//    public Channel createUniqueChannel() {
//    	try {
//    		Connection connection = connectionFactory.newConnection();
//    		Channel channel = connection.createChannel();
//
//    		System.out.println("Unique channel created: " + channel.getChannelNumber());
//
//    		return channel;
//
//    	} catch (Exception e) {
//    		throw new RuntimeException("Failed to create channel", e);
//    	}
//    } 
//    
////    public void receiveMessage() {
////    	Channel channel = connection;
//////    	try {
//////    		channel.queueDeclare(queue.getName(), false, false, false, null);
//////    	}catch(Exception ex) {
//////    		
//////    	}
////    	 
////    }
//}
