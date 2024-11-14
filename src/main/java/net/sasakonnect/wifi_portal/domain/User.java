package net.sasakonnect.wifi_portal.domain;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User extends BasePortalDomain implements Serializable,UserDetails{
	 @Column(nullable = true)
	    private String password;

	    @Column(columnDefinition = "TEXT", nullable = true)
	    private String email;

	    @Column(columnDefinition = "TEXT", nullable = true)
	    private String firstname;

	    @Column(columnDefinition = "TEXT", nullable = true)
	    private String lastname;

	    @Column(nullable = false, columnDefinition = "boolean default false")
	    private boolean isActive;
	    
	    @Column()
	    String avatorColor;

	    @Column(length = 15, unique = true, nullable = true)
	    private String phone;
	    
	    @OneToOne()
	    @JoinColumn(name="profile_image",nullable=true)
	    private UserImage profileImage;

	    @Column(nullable = true)
	    private String coupon;

	    @Column(nullable = true)
	    private String champCode;

	    @Column(nullable = true)
	    private String giftId;

	    @Column(nullable = true)
	    private String token;

	    @Column( nullable = true)
	    private String lastLogin;

	    @Column(nullable = true)
	    private String payCode;
	    
	    @Column(nullable = true)
	    private String userId;

	    @Column(nullable = true, columnDefinition = "boolean default false")
	    private boolean isMuted;

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getUsername() {
			// TODO Auto-generated method stub
			return null;
		}


}
