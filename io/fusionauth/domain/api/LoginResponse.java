package io.fusionauth.domain.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.AuthenticationThreats;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.ChangePasswordReason;
import io.fusionauth.domain.TwoFactorMethod;
import io.fusionauth.domain.User;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LoginResponse implements Buildable<LoginResponse> {
  public List<LoginPreventedResponse> actions;
  
  public String changePasswordId;
  
  public ChangePasswordReason changePasswordReason;
  
  public List<String> configurableMethods;
  
  public String emailVerificationId;
  
  public String identityVerificationId;
  
  public List<TwoFactorMethod> methods;
  
  public String pendingIdPLinkId;
  
  public String refreshToken;
  
  public UUID refreshTokenId;
  
  public String registrationVerificationId;
  
  public Map<String, Object> state;
  
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public Set<AuthenticationThreats> threatsDetected;
  
  public String token;
  
  public ZonedDateTime tokenExpirationInstant;
  
  public String trustToken;
  
  public String twoFactorId;
  
  public String twoFactorTrustId;
  
  public User user;
  
  @JacksonConstructor
  public LoginResponse() {}
  
  public LoginResponse(User paramUser, String paramString) {
    this.user = paramUser;
    this.token = paramString;
  }
  
  public LoginResponse(User paramUser) {
    this.user = paramUser;
  }
  
  public LoginResponse(List<LoginPreventedResponse> paramList) {
    this.actions = paramList;
  }
}
