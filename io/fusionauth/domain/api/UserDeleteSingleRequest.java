package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;

public class UserDeleteSingleRequest extends BaseEventRequest implements Buildable<UserDeleteSingleRequest> {
  public boolean hardDelete;
  
  @JacksonConstructor
  public UserDeleteSingleRequest() {}
  
  public UserDeleteSingleRequest(EventInfo paramEventInfo, boolean paramBoolean) {
    super(paramEventInfo);
    this.hardDelete = paramBoolean;
  }
}
