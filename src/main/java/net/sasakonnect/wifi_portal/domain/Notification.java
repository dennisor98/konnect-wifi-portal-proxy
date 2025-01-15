package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification extends BasePortalDomain {

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User receiver;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String message;
    
    @Column(nullable = false)
    private String messageType;
    
    @Column(nullable = false)
    private Boolean isPublic;
    
    @Column(columnDefinition = "TEXT", nullable = true)
    private String title;

 
}

