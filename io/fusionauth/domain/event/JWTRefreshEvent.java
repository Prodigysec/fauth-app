package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import java.util.Objects;
import java.util.UUID;

public class JWTRefreshEvent extends BaseEvent implements Buildable<JWTRefreshEvent>, ObjectIdentifiable {
  public UUID applicationId;
  
  public String original;
  
  public String refreshToken;
  
  public String token;
  
  public UUID userId;
  
  @JacksonConstructor
  public JWTRefreshEvent() {}
  
  public JWTRefreshEvent(EventInfo paramEventInfo, UUID paramUUID1, String paramString1, String paramString2, String paramString3, UUID paramUUID2) {
    super(paramEventInfo);
    this.applicationId = paramUUID1;
    this.original = paramString2;
    this.refreshToken = paramString3;
    this.token = paramString1;
    this.userId = paramUUID2;
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    JWTRefreshEvent jWTRefreshEvent = (JWTRefreshEvent)paramObject;
    return (Objects.equals(this.applicationId, jWTRefreshEvent.applicationId) && 
      Objects.equals(this.original, jWTRefreshEvent.original) && 
      Objects.equals(this.refreshToken, jWTRefreshEvent.refreshToken) && 
      Objects.equals(this.token, jWTRefreshEvent.token) && 
      Objects.equals(this.userId, jWTRefreshEvent.userId));
  }
  
  public UUID getLinkedObjectId() {
    return this.userId;
  }
  
  public void setLinkedObjectId(UUID paramUUID) {}
  
  public EventType getType() {
    return EventType.JWTRefresh;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.original, this.refreshToken, this.token, this.userId });
  }
}
