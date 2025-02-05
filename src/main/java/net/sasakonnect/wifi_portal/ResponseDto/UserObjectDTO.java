package net.sasakonnect.wifi_portal.ResponseDto;

import java.util.Date;

import lombok.Data;

@Data
public class UserObjectDTO {
    private Date deleatedAt;
    private Date updatedAt;
    private Date createdAt;
    private String user_id;
    private String email;
    private String firstname;
    private String lastname;
    private String password;
    private String is_active;
    private String phone;
    private String coupon;
    private String champCode;
    private String gift_id;
    private String token;
    private String last_login;
    private String pay_code;
    private String dev_id;
    private Boolean is_muted = false;
    private Date created_at;
    private Date updated_at;
    private Date deletedAt;
    private String avatorColor;
    private String accountType;
    private Boolean canReceivecall;
    private String avator_key;
    private String access_token;
    private String refresh_token;
}

