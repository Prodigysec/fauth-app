package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.List;

public class UserLoginIdDuplicateOnUpdateEvent extends UserLoginIdDuplicateOnCreateEvent {
  @JacksonConstructor
  public UserLoginIdDuplicateOnUpdateEvent() {}
  
  public UserLoginIdDuplicateOnUpdateEvent(EventInfo paramEventInfo, String paramString1, String paramString2, String paramString3, List<BaseUserEvent.IdentityInfo> paramList, User paramUser1, User paramUser2) {
    super(paramEventInfo, paramString1, paramString2, paramString3, paramList, paramUser1, paramUser2);
  }
  
  public EventType getType() {
    return EventType.UserLoginIdDuplicateOnUpdate;
  }
}
