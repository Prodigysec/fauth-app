package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;

public class TwoFactorUpdateRequest extends BaseEventRequest implements Buildable<TwoFactorUpdateRequest> {
  public String methodId;
  
  public String name;
  
  @JacksonConstructor
  public TwoFactorUpdateRequest() {}
  
  public TwoFactorUpdateRequest(EventInfo paramEventInfo, String paramString1, String paramString2) {
    super(paramEventInfo);
    this.methodId = paramString1;
    this.name = paramString2;
  }
}
