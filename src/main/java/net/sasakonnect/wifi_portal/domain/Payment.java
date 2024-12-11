package net.sasakonnect.wifi_portal.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment extends BasePortalDomain {
	    @ManyToOne(fetch = FetchType.LAZY) // Many payments to one user
	    @JoinColumn(name = "user_id", nullable = true) // Foreign key column
	    private User user;
	    
	    @Lob
	    @Column(name = "payment_payload", nullable = true, columnDefinition = "LONGTEXT")
	    private String paymentPayload;
	    
	    @Column(name = "txt_id", length = 255,nullable = true)
	    private String txtId;
	    
	    @Column(name = "konnect_checkout_id", length = 255,nullable = false)
	    private String konnectCheckoutId;
	    
	    @Column(nullable = false)
	    private Boolean verified; 
	    
	    @Column(nullable = true)
	    private String mobileNumber; 
	    
	    @Column(nullable = true)
	    private String deviceMac; 
	    
	    @Column(name = "is_sucessful", nullable = false)
	    private Boolean isSuccessful; 
	    
	    @Column()
	    private String idUser;
	    
	    @Lob
	    @Column(name = "payment_verification_payload")
	    private String paymentVerificationPayload;
	    
	    @ManyToOne(fetch = FetchType.LAZY)
	    @JsonIgnore
	    @JoinColumn(name = "app_id", nullable = true)
	    private App app;
}
