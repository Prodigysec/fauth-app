package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.provider.BaseIdentityProvider;
import java.util.UUID;

public class UserLoginNewDeviceEvent extends UserLoginSuccessEvent {
  @JacksonConstructor
  public UserLoginNewDeviceEvent() {}
  
  public UserLoginNewDeviceEvent(EventInfo paramEventInfo, UUID paramUUID, String paramString, BaseIdentityProvider<?> paramBaseIdentityProvider, User paramUser) {
    super(paramEventInfo, paramUUID, paramString, paramBaseIdentityProvider, paramUser);
  }
  
  public UserLoginNewDeviceEvent(EventInfo paramEventInfo, UUID paramUUID1, UUID paramUUID2, String paramString, User paramUser) {
    super(paramEventInfo, paramUUID1, paramUUID2, paramString, paramUser);
  }
  
  public EventType getType() {
    return EventType.UserLoginNewDevice;
  }
}
