package net.sasakonnect.wifi_portal.beans;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Component
@Slf4j
public class Two56BitKeyBean {
	public String encrypt(String text, String secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] decodedKey = hexStringToByteArray(secretKey);

	
		SecretKey secretKeySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		byte[] encryptedBytes = cipher.doFinal(text.getBytes());
		String encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);

		return encryptedText;
	}

	public String decrypt(String encrypted, String secretKey) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		Cipher cipher = Cipher.getInstance("AES");
		byte[] decodedKey = hexStringToByteArray(secretKey);

		
		SecretKey secretKeySpec = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encrypted));
		String decryptedText = new String(decryptedBytes);
		return decryptedText;

	}

	private static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
}
