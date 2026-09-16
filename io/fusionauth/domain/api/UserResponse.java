package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.IdentityType;
import io.fusionauth.domain.User;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserResponse {
  public String emailVerificationId;
  
  public String emailVerificationOneTimeCode;
  
  public Map<UUID, String> registrationVerificationIds;
  
  public Map<UUID, String> registrationVerificationOneTimeCodes;
  
  public String token;
  
  public ZonedDateTime tokenExpirationInstant;
  
  public User user;
  
  public List<VerificationId> verificationIds;
  
  @JacksonConstructor
  public UserResponse() {}
  
  public UserResponse(User paramUser) {
    this.user = paramUser;
  }
  
  public static class VerificationId implements Buildable<VerificationId> {
    public String id;
    
    public String oneTimeCode;
    
    public IdentityType type;
    
    public String value;
  }
}
