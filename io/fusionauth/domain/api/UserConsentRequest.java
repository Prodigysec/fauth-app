package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.UserConsent;

public class UserConsentRequest {
  public UserConsent userConsent;
  
  @JacksonConstructor
  public UserConsentRequest() {}
  
  public UserConsentRequest(UserConsent paramUserConsent) {
    this.userConsent = paramUserConsent;
  }
}
