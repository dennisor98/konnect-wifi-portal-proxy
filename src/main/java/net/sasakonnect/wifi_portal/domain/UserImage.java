package net.sasakonnect.wifi_portal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserImage extends BasePortalDomain{
   @OneToOne(fetch=FetchType.LAZY)
   @JoinColumn(name="user_id")
   User user;
   
   @Column(columnDefinition = "LONGTEXT")
   String image;
}
