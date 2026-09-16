package io.fusionauth.domain.api.jwt;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseEventRequest;

public class RefreshRequest extends BaseEventRequest implements Buildable<RefreshRequest> {
  public String refreshToken;
  
  public Integer timeToLiveInSeconds;
  
  public String token;
  
  @JacksonConstructor
  public RefreshRequest() {}
  
  public RefreshRequest(String paramString) {
    this.refreshToken = paramString;
  }
  
  public RefreshRequest(EventInfo paramEventInfo, String paramString) {
    super(paramEventInfo);
    this.refreshToken = paramString;
  }
}
