package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.PasswordType;
import io.fusionauth.domain.SendSetPasswordIdentityType;
import io.fusionauth.domain.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRequest extends BaseEventRequest implements Buildable<UserRequest> {
  public UUID applicationId;
  
  public String currentPassword;
  
  public boolean disableDomainBlock;
  
  public PasswordType passwordFieldType = PasswordType.PLAINTEXT;
  
  @Deprecated
  public boolean sendSetPasswordEmail;
  
  public SendSetPasswordIdentityType sendSetPasswordIdentityType;
  
  public boolean skipVerification;
  
  public User user;
  
  public List<String> verificationIds = new ArrayList<>();
  
  @JacksonConstructor
  public UserRequest() {}
  
  public UserRequest(User paramUser) {
    this.sendSetPasswordEmail = false;
    this.skipVerification = true;
    this.user = paramUser;
  }
  
  public UserRequest(boolean paramBoolean1, boolean paramBoolean2, User paramUser) {
    this.sendSetPasswordEmail = paramBoolean1;
    this.skipVerification = paramBoolean2;
    this.user = paramUser;
  }
  
  public UserRequest(EventInfo paramEventInfo, User paramUser) {
    super(paramEventInfo);
    this.sendSetPasswordEmail = false;
    this.skipVerification = true;
    this.user = paramUser;
  }
  
  public UserRequest(EventInfo paramEventInfo, UUID paramUUID, boolean paramBoolean1, boolean paramBoolean2, String paramString, User paramUser) {
    super(paramEventInfo);
    this.applicationId = paramUUID;
    this.currentPassword = paramString;
    this.sendSetPasswordEmail = paramBoolean1;
    this.skipVerification = paramBoolean2;
    this.user = paramUser;
  }
}
