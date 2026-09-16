package io.fusionauth.domain.event;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.email.Email;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserActionEvent extends BaseEvent implements Buildable<UserActionEvent>, ObjectIdentifiable {
  public static ZonedDateTime Infinite = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.MAX_VALUE), ZoneOffset.UTC);
  
  public final List<UUID> applicationIds = new ArrayList<>();
  
  public String action;
  
  public UUID actionId;
  
  public UUID actioneeUserId;
  
  public UUID actionerUserId;
  
  public String comment;
  
  public Email email;
  
  public boolean emailedUser;
  
  public ZonedDateTime expiry;
  
  public String localizedAction;
  
  public String localizedDuration;
  
  public String localizedOption;
  
  public String localizedReason;
  
  public boolean notifyUser;
  
  public String option;
  
  public UserActionPhase phase;
  
  public String reason;
  
  public String reasonCode;
  
  @JacksonConstructor
  public UserActionEvent() {}
  
  public UserActionEvent(EventInfo paramEventInfo, UUID paramUUID1, UUID paramUUID2, UUID paramUUID3, List<UUID> paramList, String paramString1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, ZonedDateTime paramZonedDateTime, String paramString8, UserActionPhase paramUserActionPhase, String paramString9, boolean paramBoolean1, boolean paramBoolean2, Email paramEmail) {
    super(paramEventInfo);
    this.actionId = paramUUID1;
    this.action = paramString1;
    this.actioneeUserId = paramUUID2;
    this.actionerUserId = paramUUID3;
    this.comment = paramString9;
    this.expiry = paramZonedDateTime;
    this.localizedAction = paramString2;
    this.localizedDuration = paramString8;
    this.localizedOption = paramString4;
    this.localizedReason = paramString6;
    this.notifyUser = paramBoolean1;
    this.option = paramString3;
    this.phase = paramUserActionPhase;
    this.reason = paramString5;
    this.reasonCode = paramString7;
    this.emailedUser = paramBoolean2;
    this.email = paramEmail;
    if (paramList != null) {
      this.applicationIds.addAll(paramList);
      Collections.sort(this.applicationIds);
    } 
  }
  
  public boolean active() {
    return (this.expiry != null && ZonedDateTime.now(ZoneOffset.UTC).isBefore(this.expiry));
  }
  
  public boolean equals(Object paramObject) {
    if (!super.equals(paramObject))
      return false; 
    UserActionEvent userActionEvent = (UserActionEvent)paramObject;
    return (Objects.equals(this.applicationIds, userActionEvent.applicationIds) && 
      Objects.equals(this.action, userActionEvent.action) && 
      Objects.equals(this.actionId, userActionEvent.actionId) && 
      Objects.equals(this.actioneeUserId, userActionEvent.actioneeUserId) && 
      Objects.equals(this.actionerUserId, userActionEvent.actionerUserId) && 
      Objects.equals(this.comment, userActionEvent.comment) && 
      Objects.equals(this.email, userActionEvent.email) && 
      Objects.equals(Boolean.valueOf(this.emailedUser), Boolean.valueOf(userActionEvent.emailedUser)) && 
      Objects.equals(this.expiry, userActionEvent.expiry) && 
      Objects.equals(this.localizedAction, userActionEvent.localizedAction) && 
      Objects.equals(this.localizedDuration, userActionEvent.localizedDuration) && 
      Objects.equals(this.localizedOption, userActionEvent.localizedOption) && 
      Objects.equals(this.localizedReason, userActionEvent.localizedReason) && 
      Objects.equals(Boolean.valueOf(this.notifyUser), Boolean.valueOf(userActionEvent.notifyUser)) && 
      Objects.equals(this.option, userActionEvent.option) && 
      Objects.equals(this.phase, userActionEvent.phase) && 
      Objects.equals(this.reason, userActionEvent.reason) && 
      Objects.equals(this.reasonCode, userActionEvent.reasonCode));
  }
  
  public UUID getLinkedObjectId() {
    return this.actioneeUserId;
  }
  
  public void setLinkedObjectId(UUID paramUUID) {}
  
  public EventType getType() {
    return EventType.UserAction;
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          Integer.valueOf(super.hashCode()), this.actionId, this.applicationIds, this.action, this.actioneeUserId, this.actionerUserId, this.comment, this.email, this.expiry, this.localizedAction, 
          this.localizedDuration, this.localizedOption, this.localizedReason, 
          Boolean.valueOf(this.notifyUser), this.option, Boolean.valueOf(this.emailedUser), this.phase, this.reason, this.reasonCode });
  }
}
