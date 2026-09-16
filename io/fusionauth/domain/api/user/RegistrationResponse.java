package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import io.fusionauth.domain.api.UserResponse;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class RegistrationResponse {
  public String refreshToken;
  
  public UUID refreshTokenId;
  
  public UserRegistration registration;
  
  public String registrationVerificationId;
  
  public String registrationVerificationOneTimeCode;
  
  public String token;
  
  public ZonedDateTime tokenExpirationInstant;
  
  public User user;
  
  public List<UserResponse.VerificationId> verificationIds;
  
  @JacksonConstructor
  public RegistrationResponse() {}
  
  public RegistrationResponse(User paramUser, UserRegistration paramUserRegistration) {
    this.user = paramUser;
    this.registration = paramUserRegistration;
  }
}
