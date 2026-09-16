package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseEventRequest;

public class RegistrationDeleteRequest extends BaseEventRequest implements Buildable<RegistrationDeleteRequest> {
  @JacksonConstructor
  public RegistrationDeleteRequest() {}
  
  public RegistrationDeleteRequest(EventInfo paramEventInfo) {
    super(paramEventInfo);
  }
}
