package net.sasakonnect.wifi_portal.services;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.sasakonnect.wifi_portal.domain.User;
import net.sasakonnect.wifi_portal.enums.JwtType;


@Slf4j
@Service
public class JwtService {
	@Value("${JWT_EXPIRY_TIME:604800000}")
	Long jwtExpiryTime;

	@Value("${REFRESH_JWT_EXPIRY_TIME:691200000}")
	Long jwtRefreshExpiryTime;
	@Value("${OPENID_JWT_EXPIRY_TIME:360000}")
	Long openIdJwtExpiryTime;
	
	@Value("${JWT_SECRET}")
	String jwtSecret;

	byte[] decodedKey = null;
	SecretKeySpec secretKey = null;

	@PostConstruct()
	void init() {
		decodedKey = Base64.getDecoder().decode(jwtSecret);
		secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HMACSHA256");
	}

	public String generateToken(User user) {
		try {
			Map<String, Object> claims = new HashMap<>();
			claims.put("id", user.getId());
			claims.put("token_type", JwtType.ACCESS_TOKEN.getToken());
			claims.put("firstName", user.getFirstname());
			return Jwts.builder().setClaims(claims).setSubject(user.getId().toString()).setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + jwtExpiryTime))// 10 days validity
					.setId(UUID.randomUUID().toString())
					.signWith(secretKey, SignatureAlgorithm.HS256).compact();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String generateAdminToken(User user) {
		try {
			Map<String, Object> claims = new HashMap<>();
			claims.put("id", user.getId());
			claims.put("token_type",JwtType.ADMIN_ACCESS_TOKEN.getToken());

			claims.put("firstName", user.getFirstname());
			return Jwts.builder().setClaims(claims).setSubject(user.getId().toString()).setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + jwtExpiryTime))// 10 days validity
					.setId(UUID.randomUUID().toString())
					.signWith(secretKey, SignatureAlgorithm.HS256).compact();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String generateTokenForWindow(User user) {
		try {
			Map<String, Object> claims = new HashMap<>();
			claims.put("id", user.getId());
			claims.put("token_type", "access_token");

			claims.put("firstName", user.getFirstname());
			return Jwts.builder().setClaims(claims).setSubject(user.getId().toString()).setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + 60000))// 10 days validity
					.setId(UUID.randomUUID().toString())

					.signWith(secretKey, SignatureAlgorithm.HS256).compact();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String generateRefreshToken(User user) {
		try {
			Map<String, Object> claims = new HashMap<>();
			claims.put("id", user.getId());
			claims.put("token_type", "refresh_token");
			claims.put("firstName", user.getFirstname());
			return Jwts.builder().setClaims(claims).setSubject(user.getId().toString()).setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiryTime))// validity
					.setId(UUID.randomUUID().toString())
					.signWith(secretKey, SignatureAlgorithm.HS256).compact();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean validateToken(String token, UserDetails userDetails) {
		try {
			final String username = extractUsername(token);
			System.out.println(username);
			System.out.println(userDetails.getUsername());
			User user = (User) userDetails;
			return (username.equals(user.getId()) && !isTokenExpired(token));
		} catch (MalformedJwtException e) {
			e.printStackTrace();
		} catch (Exception e) {
			log.error(e.getMessage());

		}
		return false;

	}

	public boolean validateToken(String token, User userDetails, JwtType jwt) {
		try {
			final String username = extractUsername(token, jwt);

			User user = userDetails;
			return (username.equals(user.getId()) && !isTokenExpired(token));
		} catch (MalformedJwtException e) {
			e.printStackTrace();
		}
		return false;

	}

	public String extractUsername(String token) throws MalformedJwtException {
		try {
			if (Jwts.parserBuilder().setSigningKey(secretKey).build().isSigned(token)) {
				Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();

				return claims.getSubject();

			} else {
				return null;
			}

		} catch (MalformedJwtException e) {
			throw e;
		}

	}

	public String extractUsername(String token, JwtType jwt) throws MalformedJwtException, UnsupportedJwtException {
		try {
			if (Jwts.parserBuilder().setSigningKey(secretKey).build().isSigned(token)) {

				Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
				if (claims.get("token_type") == jwt.getToken()) {
					throw new UnsupportedJwtException("jwt supplied is not supported ");
				}
				return claims.getSubject();

			} else {
				return null;
			}

		} catch (MalformedJwtException e) {
			System.out.println("exception");
			throw e;
		}

	}

	private Date extractExpiration(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
		return claims.getExpiration();
	}

	private boolean isTokenExpired(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();

		return claims.getExpiration().before(new Date());
	}



}

