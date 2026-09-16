package io.fusionauth.domain.api.identityProvider;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseLoginRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IdentityProviderStartLoginRequest extends BaseLoginRequest implements Buildable<IdentityProviderStartLoginRequest> {
  public String connectionTestId;
  
  public Map<String, String> data;
  
  public UUID identityProviderId;
  
  public String loginId;
  
  public List<String> loginIdTypes = new ArrayList<>();
  
  public Map<String, Object> state;
  
  @JacksonConstructor
  public IdentityProviderStartLoginRequest() {}
  
  public IdentityProviderStartLoginRequest(UUID paramUUID1, Map<String, String> paramMap, UUID paramUUID2) {
    this.applicationId = paramUUID1;
    this.data = paramMap;
    this.identityProviderId = paramUUID2;
  }
  
  public IdentityProviderStartLoginRequest(UUID paramUUID1, UUID paramUUID2, String paramString1, String paramString2) {
    this.applicationId = paramUUID1;
    this.identityProviderId = paramUUID2;
    this.loginId = paramString1;
    if (paramString2 != null) {
      this.eventInfo = (this.eventInfo != null) ? this.eventInfo : new EventInfo();
      this.eventInfo.ipAddress = paramString2;
    } 
  }
  
  public IdentityProviderStartLoginRequest(UUID paramUUID1, UUID paramUUID2, String paramString1, String paramString2, Map<String, Object> paramMap) {
    this.applicationId = paramUUID1;
    this.identityProviderId = paramUUID2;
    this.loginId = paramString1;
    this.state = paramMap;
    if (paramString2 != null) {
      this.eventInfo = (this.eventInfo != null) ? this.eventInfo : new EventInfo();
      this.eventInfo.ipAddress = paramString2;
    } 
  }
}
