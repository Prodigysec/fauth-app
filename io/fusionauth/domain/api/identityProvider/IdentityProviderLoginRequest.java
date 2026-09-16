package io.fusionauth.domain.api.identityProvider;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseLoginRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IdentityProviderLoginRequest extends BaseLoginRequest implements Buildable<IdentityProviderLoginRequest> {
  public String connectionTestId;
  
  public Map<String, String> data = new HashMap<>(1);
  
  public UUID identityProviderId;
  
  public boolean noLink;
  
  @JacksonConstructor
  public IdentityProviderLoginRequest() {}
  
  public IdentityProviderLoginRequest(EventInfo paramEventInfo) {
    super(paramEventInfo);
  }
  
  public IdentityProviderLoginRequest addData(String paramString1, String paramString2) {
    if (paramString2 == null)
      return this; 
    this.data.put(paramString1, paramString2);
    return this;
  }
  
  public String getEncodedJWT() {
    return this.data.get("token");
  }
  
  public void setEncodedJWT(String paramString) {
    this.data.put("token", paramString);
  }
}
