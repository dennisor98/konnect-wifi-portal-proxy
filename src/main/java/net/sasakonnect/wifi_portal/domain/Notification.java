package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "notifications")
public class Notification extends BasePortalDomain {

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User sender;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String message;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String title;

    @ManyToMany
    @JoinTable(
        name = "user_notified",
        joinColumns = @JoinColumn(name = "notification_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> userNotified;

    // Getters and setters
}

