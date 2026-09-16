package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.BaseEventRequest;
import java.util.ArrayList;
import java.util.List;

public class RegistrationRequest extends BaseEventRequest {
  public boolean disableDomainBlock;
  
  public boolean generateAuthenticationToken;
  
  public UserRegistration registration;
  
  @Deprecated
  public boolean sendSetPasswordEmail;
  
  public SendSetPasswordIdentityType sendSetPasswordIdentityType;
  
  public boolean skipRegistrationVerification;
  
  public boolean skipVerification;
  
  public User user;
  
  public List<String> verificationIds = new ArrayList<>();
  
  @JacksonConstructor
  public RegistrationRequest() {}
  
  public RegistrationRequest(User paramUser, UserRegistration paramUserRegistration) {
    this.user = paramUser;
    this.registration = paramUserRegistration;
  }
  
  public RegistrationRequest(User paramUser, UserRegistration paramUserRegistration, boolean paramBoolean1, boolean paramBoolean2) {
    this.user = paramUser;
    this.registration = paramUserRegistration;
    this.sendSetPasswordEmail = paramBoolean1;
    this.skipVerification = paramBoolean2;
  }
  
  public RegistrationRequest(EventInfo paramEventInfo, User paramUser, UserRegistration paramUserRegistration) {
    super(paramEventInfo);
    this.user = paramUser;
    this.registration = paramUserRegistration;
  }
  
  public RegistrationRequest(EventInfo paramEventInfo, User paramUser, UserRegistration paramUserRegistration, boolean paramBoolean1, boolean paramBoolean2) {
    super(paramEventInfo);
    this.user = paramUser;
    this.registration = paramUserRegistration;
    this.sendSetPasswordEmail = paramBoolean1;
    this.skipVerification = paramBoolean2;
  }
}
