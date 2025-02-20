package net.sasakonnect.wifi_portal.services;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.sasakonnect.wifi_portal.RequestDto.CodeRefDto;
import net.sasakonnect.wifi_portal.domain.ReferralCode;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.domain.UserReferal;
import net.sasakonnect.wifi_portal.repository.ReferralCodeRepository;
import net.sasakonnect.wifi_portal.repository.UserReferralRepository;
import net.sasakonnect.wifi_portal.repository.UserRepository;

@Service
public class PromotionService {
	@Autowired
	UserRepository userRepository;
	@Autowired
	ReferralCodeRepository referalRepository;
	@Autowired
	UserReferralRepository userReferalRepository;
	private static final String UPPERCASE = "ABCDEFGHIJKLMNPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnpqrstuvwxyz";
    private static final String DIGITS = "123456789";
    private static final String CHARACTERS = UPPERCASE + LOWERCASE + DIGITS;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_LENGTH = 6;
   public Object generateReferalCode() {
	   User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	   Optional<ReferralCode> codeOpt = this.referalRepository.findByUser(user);
	   if(codeOpt.isPresent()) {
		   ObjectNode res =  JsonNodeFactory.instance.objectNode();
		   res.put("success",false);
		   res.put("message","You already have a referal code");
		   
		   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
	   }
	   String refCode = generateUniqueRefCode().toUpperCase();
	   var refBuild = ReferralCode.builder().code(refCode).isActive(true).user(user).build();
	   
	   try {
		   this.referalRepository.save(refBuild);
		   user.setRefCode(refCode);
		   userRepository.save(user);
		   ObjectNode res = JsonNodeFactory.instance.objectNode();
		   res.put("success",true);
		   res.put("message","Referal Code generated!");
		   res.put("code",refCode);
		   
		   return ResponseEntity.status(HttpStatus.OK).body(res);
	   }catch(Exception ex) {
		   ex.printStackTrace();
		   ObjectNode res = JsonNodeFactory.instance.objectNode();
		   res.put("success", false);
		   res.put("message","A server error occured");
		   
		   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
	   }
	   
   }
   
   public Object AddRefererCode(CodeRefDto refDto) {
	   User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	   Optional<ReferralCode> refCodeOpt  = this.referalRepository.findByCode(refDto.getRefCode());
	   if(refCodeOpt.isEmpty()) {
		   ObjectNode res =  JsonNodeFactory.instance.objectNode();
		   res.put("success",false);
		   res.put("message","Invalid referal code");
		   
		   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
	   }
	   var refCode = refCodeOpt.get();
	   if(!refCode.getIsActive()) {
		   ObjectNode res =  JsonNodeFactory.instance.objectNode();
		   res.put("success",false);
		   res.put("message","Referal code is inactive");
		   
		   return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
	   }
	   if(refCode.getUser().getId().equalsIgnoreCase(user.getId())) {
		   ObjectNode res =  JsonNodeFactory.instance.objectNode();
		   res.put("success",false);
		   res.put("message","Unallowed operation");
		   return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
	   }
	   
	   Optional<UserReferal> userHasRef = this.userReferalRepository.findByUser(user); 
	   if(userHasRef.isPresent()) {
		   ObjectNode res =  JsonNodeFactory.instance.objectNode();
		   res.put("success",false);
		   res.put("message","You were already refered");
		   return ResponseEntity.status(HttpStatus.CONFLICT).body(res);
	   }
	   
	   var refBuild = UserReferal.builder().referer(refCode).user(user).build();
	   try {
		   this.userReferalRepository.save(refBuild);
		   user.setIsRefered(true);
		   userRepository.save(user);
		   ObjectNode res =  JsonNodeFactory.instance.objectNode();
		   res.put("success",true);
		   res.put("message","Referal code added");
		   return ResponseEntity.status(HttpStatus.OK).body(res);
	   }catch(Exception ex) {
		   
	   }
	   
	   
	   return null;
   }
   
   public static String generateUniqueRefCode() {
       String uuid = UUID.randomUUID().toString().replace("-", "");

       StringBuilder sb = new StringBuilder();

       sb.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
       sb.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
       sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));

       while (sb.length() < CODE_LENGTH) {
           sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
       }

       return shuffleString(sb.toString());
   }

   private static String shuffleString(String input) {
       char[] characters = input.toCharArray();
       for (int i = characters.length - 1; i > 0; i--) {
           int j = RANDOM.nextInt(i + 1);
           char temp = characters[i];
           characters[i] = characters[j];
           characters[j] = temp;
       }
       return new String(characters);
   }

}
