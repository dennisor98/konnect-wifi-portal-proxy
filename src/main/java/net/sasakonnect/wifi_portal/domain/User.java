package net.sasakonnect.wifi_portal.domain;

import java.io.Serializable;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = "profileImage")
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
	    
	    @Column(nullable=true)
	    String appVersion;

	    @Column(length = 15, unique = true, nullable = true)
	    private String phone;
	    @JsonIgnore()
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
	    
	    @Column(nullable=true)
	    private String devId;

	    @Column(nullable = true, columnDefinition = "boolean default false")
	    private boolean isMuted;
	    
	    @Column(columnDefinition = "boolean default false")
	    Boolean isRefered;
	    
	    @Column(nullable=true)
	    String refCode;

	    
	    @JsonIgnore()
		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			// TODO Auto-generated method stub
			return null;
		}

		@JsonIgnore()
		@Override
		public String getUsername() {
			// TODO Auto-generated method stub
			return null;
		}
		@JsonIgnore
		@Override
		public boolean isAccountNonExpired() { return true; }

		@JsonIgnore
		@Override
		public boolean isAccountNonLocked() { return true; }

		@JsonIgnore
		@Override
		public boolean isCredentialsNonExpired() { return true; }

		@JsonIgnore
		@Override
		public boolean isEnabled() { return this.isActive; }



}
