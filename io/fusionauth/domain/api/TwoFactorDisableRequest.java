package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import java.util.UUID;

public class TwoFactorDisableRequest extends BaseEventRequest implements Buildable<TwoFactorDisableRequest> {
  public UUID applicationId;
  
  public String code;
  
  public String methodId;
  
  @JacksonConstructor
  public TwoFactorDisableRequest() {}
  
  public TwoFactorDisableRequest(EventInfo paramEventInfo, UUID paramUUID, String paramString1, String paramString2) {
    super(paramEventInfo);
    this.applicationId = paramUUID;
    this.code = paramString1;
    this.methodId = paramString2;
  }
}
