package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Consent;
import java.util.List;

public class ConsentResponse {
  public Consent consent;
  
  public List<Consent> consents;
  
  @JacksonConstructor
  public ConsentResponse() {}
  
  public ConsentResponse(List<Consent> paramList) {
    this.consents = paramList;
  }
  
  public ConsentResponse(Consent paramConsent) {
    this.consent = paramConsent;
  }
}
