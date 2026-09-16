package io.fusionauth.app.service.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.User;
import io.fusionauth.domain.UserRegistration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Deprecated
public class LegacyRegistrationState {
  public Map<UUID, List<String>> consents;
  
  public int[] formSteps;
  
  public UUID identityProviderId;
  
  public UserRegistration registration;
  
  public User user;
  
  public LegacyRegistrationState(User paramUser, UserRegistration paramUserRegistration, Map<UUID, List<String>> paramMap, int[] paramArrayOfint, UUID paramUUID) {
    this.user = paramUser;
    this.registration = paramUserRegistration;
    this.consents = paramMap;
    this.formSteps = paramArrayOfint;
    this.identityProviderId = paramUUID;
  }
  
  @JacksonConstructor
  private LegacyRegistrationState() {}
}
