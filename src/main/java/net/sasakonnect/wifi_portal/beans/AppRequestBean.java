package net.sasakonnect.wifi_portal.beans;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import lombok.Data;
import net.sasakonnect.wifi_portal.domain.App;

@Component
@RequestScope
@Data
public class AppRequestBean {
	App app;

}
