package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Consent;

public class ConsentRequest {
  public Consent consent;
  
  @JacksonConstructor
  public ConsentRequest() {}
  
  public ConsentRequest(Consent paramConsent) {
    this.consent = paramConsent;
  }
}
