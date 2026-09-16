package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserConsent;
import java.util.List;

public class UserConsentResponse {
  public UserConsent userConsent;
  
  public List<UserConsent> userConsents;
  
  @JacksonConstructor
  public UserConsentResponse() {}
  
  public UserConsentResponse(UserConsent paramUserConsent) {
    this.userConsent = paramUserConsent;
  }
  
  public UserConsentResponse(List<UserConsent> paramList) {
    this.userConsents = paramList;
  }
}
