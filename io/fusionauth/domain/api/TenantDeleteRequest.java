package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;

public class TenantDeleteRequest extends BaseEventRequest implements Buildable<TenantDeleteRequest> {
  public boolean async;
  
  @JacksonConstructor
  public TenantDeleteRequest() {}
  
  public TenantDeleteRequest(EventInfo paramEventInfo, boolean paramBoolean) {
    super(paramEventInfo);
    this.async = paramBoolean;
  }
}
