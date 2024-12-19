//package net.sasakonnect.wifi_portal.services;
//
//import java.util.concurrent.TimeUnit;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.ListOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//
//import jakarta.annotation.Resource;
//
//@Service
//public class RedisService {
//	 @Autowired
//	 private RedisTemplate<String, Object> redisTemplate;
//	@Resource(name="redisTemplate")
//	private ListOperations<String, String> listOps;
//	
//	public void addTransactionIten(String transactionId) {
//		redisTemplate.opsForValue().set("transId"+transactionId,transactionId);
//		redisTemplate.expire("transId"+transactionId, 60, TimeUnit.SECONDS);
//	}
//	
//	
//	public String getItemFromRedis(String key) {
//        return (String) redisTemplate.opsForValue().get(key);
//    }
//	
//	public void removeItemByKey() {
//		
//	}
//	
//	
//	public Object getItemByKey(String key) {
//		  return (String) redisTemplate.opsForValue().get(key);
//	}
//	  
//	  
//}
