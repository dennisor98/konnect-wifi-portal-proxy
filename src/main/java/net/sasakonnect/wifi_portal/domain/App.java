package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class App extends BasePortalDomain implements Serializable {

    private static final long serialVersionUID = 1L;

	@Column(unique = true, nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String appKey;

    @Column(nullable = false)
    private String appSecret;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(nullable = false)
    private String callbackUrl;
    
    // M-Pesa credentials
    @Column(nullable = true)
    private String consumerKey;

    @Column(nullable = true)
    private String consumerSecret;

    @Column(nullable = true)
    private String passkey;

    @Column(nullable = true)
    private String businessShortCode;

    @Column(nullable = true)
    private String mpesaTillNo;
    @JsonIgnore
    @OneToMany(mappedBy = "app", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
    
}
