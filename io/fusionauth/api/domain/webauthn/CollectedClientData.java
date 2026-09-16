package io.fusionauth.api.domain.webauthn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.fusionauth.domain.Buildable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectedClientData implements Buildable<CollectedClientData> {
  public String challenge;
  
  public boolean crossOrigin;
  
  public String origin;
  
  public TokenBinding tokenBinding;
  
  public String type;
}
