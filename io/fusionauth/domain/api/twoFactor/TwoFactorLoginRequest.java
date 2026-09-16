package io.fusionauth.domain.api.twoFactor;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseLoginRequest;
import java.util.UUID;

public class TwoFactorLoginRequest extends BaseLoginRequest implements Buildable<TwoFactorLoginRequest> {
  public String code;
  
  public boolean trustComputer;
  
  public String twoFactorId;
  
  public UUID userId;
  
  @JacksonConstructor
  public TwoFactorLoginRequest() {}
  
  public TwoFactorLoginRequest(UUID paramUUID, String paramString1, String paramString2) {
    this.applicationId = paramUUID;
    this.code = paramString1;
    this.twoFactorId = paramString2;
  }
  
  public TwoFactorLoginRequest(EventInfo paramEventInfo, UUID paramUUID, String paramString1, String paramString2) {
    super(paramEventInfo);
    this.applicationId = paramUUID;
    this.code = paramString1;
    this.twoFactorId = paramString2;
  }
  
  @Deprecated
  public TwoFactorLoginRequest(UUID paramUUID, String paramString1, String paramString2, String paramString3) {
    this.applicationId = paramUUID;
    this.code = paramString1;
    this.twoFactorId = paramString2;
    if (paramString3 != null) {
      this.eventInfo = (this.eventInfo != null) ? this.eventInfo : new EventInfo();
      this.eventInfo.ipAddress = paramString3;
    } 
  }
}
