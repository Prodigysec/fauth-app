package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.User;
import io.fusionauth.domain.jwt.RefreshToken;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;

public class JWTRefreshTokenRevokeEvent extends BaseEvent implements Buildable<JWTRefreshTokenRevokeEvent>, ObjectIdentifiable {
  public final User user;
  
  public UUID applicationId;
  
  public Map<UUID, Integer> applicationTimeToLiveInSeconds = new TreeMap<>();
  
  public RefreshToken refreshToken;
  
  public UUID userId;
  
  public JWTRefreshTokenRevokeEvent(EventInfo paramEventInfo, User paramUser, UUID paramUUID, int paramInt) {
    super(paramEventInfo);
    this.applicationId = paramUUID;
    this.applicationTimeToLiveInSeconds.put(paramUUID, Integer.valueOf(paramInt));
    this.user = (paramUser != null) ? (new User(paramUser)).secure().sort() : null;
    this.userId = (paramUser == null) ? null : paramUser.id;
  }
  
  public JWTRefreshTokenRevokeEvent(EventInfo paramEventInfo, User paramUser, Map<UUID, Integer> paramMap) {
    super(paramEventInfo);
    this.applicationTimeToLiveInSeconds.putAll(paramMap);
    this.user = (paramUser != null) ? (new User(paramUser)).secure().sort() : null;
    this.userId = (paramUser == null) ? null : paramUser.id;
  }
  
  @JacksonConstructor
  public JWTRefreshTokenRevokeEvent() {
    this.user = null;
  }
  
  public List<UUID> applicationIds() {
    return new ArrayList<>(this.applicationTimeToLiveInSeconds.keySet());
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    JWTRefreshTokenRevokeEvent jWTRefreshTokenRevokeEvent = (JWTRefreshTokenRevokeEvent)paramObject;
    return (Objects.equals(this.applicationId, jWTRefreshTokenRevokeEvent.applicationId) && 
      Objects.equals(this.applicationTimeToLiveInSeconds, jWTRefreshTokenRevokeEvent.applicationTimeToLiveInSeconds) && 
      Objects.equals(this.user, jWTRefreshTokenRevokeEvent.user) && 
      Objects.equals(this.userId, jWTRefreshTokenRevokeEvent.userId));
  }
  
  public UUID getLinkedObjectId() {
    return this.userId;
  }
  
  public void setLinkedObjectId(UUID paramUUID) {}
  
  public EventType getType() {
    return EventType.JWTRefreshTokenRevoke;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { Integer.valueOf(super.hashCode()), this.applicationId, this.applicationTimeToLiveInSeconds, this.user, this.userId });
  }
}
