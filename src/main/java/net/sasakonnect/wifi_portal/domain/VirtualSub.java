package net.sasakonnect.wifi_portal.domain;

import java.io.Serializable;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
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
public class VirtualSub extends BasePortalDomain implements Serializable{
	private static final long serialVersionUID = 8106501304964450466L;
	
	@ManyToOne()
	@JoinColumn(name="user_id")
	@OnDelete(action=OnDeleteAction.SET_NULL)
	User user;
	
	@Column()
	String name;
	
	@Column()
	String subId;
	
	@Column()
	String amount;
	
	@Column()
	String packageId;
	
	@Column()
	Boolean isActive;

}
