package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.api.BaseEventRequest;
import java.util.List;
import java.util.UUID;

public class ChangePasswordRequest extends BaseEventRequest implements Buildable<ChangePasswordRequest> {
  public UUID applicationId;
  
  public String changePasswordId;
  
  public String currentPassword;
  
  public String loginId;
  
  public List<String> loginIdTypes;
  
  public String password;
  
  public String refreshToken;
  
  public String trustChallenge;
  
  public String trustToken;
  
  @JacksonConstructor
  public ChangePasswordRequest() {}
  
  public ChangePasswordRequest(String paramString) {
    this.password = paramString;
  }
  
  public ChangePasswordRequest(String paramString1, String paramString2, String paramString3) {
    this.currentPassword = paramString2;
    this.loginId = paramString1;
    this.password = paramString3;
  }
}
