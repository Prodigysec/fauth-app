package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import java.util.List;
import java.util.Objects;

public class UserLoginIdDuplicateOnCreateEvent extends BaseUserEvent implements Buildable<UserLoginIdDuplicateOnCreateEvent>, NonTransactionalEvent {
  public final User existing;
  
  public String duplicateEmail;
  
  public List<BaseUserEvent.IdentityInfo> duplicateIdentities;
  
  public String duplicatePhoneNumber;
  
  public String duplicateUsername;
  
  @JacksonConstructor
  public UserLoginIdDuplicateOnCreateEvent() {
    this.existing = null;
  }
  
  public UserLoginIdDuplicateOnCreateEvent(EventInfo paramEventInfo, String paramString1, String paramString2, String paramString3, List<BaseUserEvent.IdentityInfo> paramList, User paramUser1, User paramUser2) {
    super(paramEventInfo, paramUser2);
    this.duplicateEmail = paramString1;
    this.duplicateUsername = paramString2;
    this.duplicatePhoneNumber = paramString3;
    this.duplicateIdentities = paramList;
    this.existing = (new User(paramUser1)).secure().sort();
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserLoginIdDuplicateOnCreateEvent userLoginIdDuplicateOnCreateEvent = (UserLoginIdDuplicateOnCreateEvent)paramObject;
    return (Objects.equals(this.duplicateEmail, userLoginIdDuplicateOnCreateEvent.duplicateEmail) && 
      Objects.equals(this.duplicateIdentities, userLoginIdDuplicateOnCreateEvent.duplicateIdentities) && 
      Objects.equals(this.duplicatePhoneNumber, userLoginIdDuplicateOnCreateEvent.duplicatePhoneNumber) && 
      Objects.equals(this.duplicateUsername, userLoginIdDuplicateOnCreateEvent.duplicateUsername) && 
      Objects.equals(this.existing, userLoginIdDuplicateOnCreateEvent.existing));
  }
  
  public EventType getType() {
    return EventType.UserLoginIdDuplicateOnCreate;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.duplicateEmail, this.duplicateIdentities, this.duplicatePhoneNumber, this.duplicateUsername, this.existing });
  }
}
