package io.fusionauth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inversoft.json.ToString;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class UserActionLog implements Buildable<UserActionLog> {
  public UUID actioneeUserId;
  
  public UUID actionerUserId;
  
  public List<UUID> applicationIds = new ArrayList<>();
  
  public String comment;
  
  public boolean emailUserOnEnd;
  
  public Boolean endEventSent;
  
  public ZonedDateTime expiry;
  
  public LogHistory history;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public String localizedName;
  
  public String localizedOption;
  
  public String localizedReason;
  
  public String name;
  
  public boolean notifyUserOnEnd;
  
  public String option;
  
  public String reason;
  
  public String reasonCode;
  
  public UUID userActionId;
  
  public UserActionLog() {}
  
  public UserActionLog(UUID paramUUID1, UUID paramUUID2, UUID paramUUID3, List<UUID> paramList, String paramString1, ZonedDateTime paramZonedDateTime1, String paramString2, String paramString3, String paramString4, String paramString5, String paramString6, String paramString7, String paramString8, ZonedDateTime paramZonedDateTime2, Boolean paramBoolean, LogHistory paramLogHistory, boolean paramBoolean1, boolean paramBoolean2) {
    this.actioneeUserId = paramUUID1;
    this.actionerUserId = paramUUID2;
    this.userActionId = paramUUID3;
    if (paramList != null)
      this.applicationIds = paramList; 
    this.comment = paramString1;
    this.expiry = paramZonedDateTime1;
    this.name = paramString2;
    this.localizedName = paramString3;
    this.option = paramString4;
    this.reason = paramString6;
    this.reasonCode = paramString8;
    this.insertInstant = paramZonedDateTime2;
    this.endEventSent = paramBoolean;
    this.history = paramLogHistory;
    this.localizedOption = paramString5;
    this.localizedReason = paramString7;
    this.emailUserOnEnd = paramBoolean2;
    this.notifyUserOnEnd = paramBoolean1;
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof UserActionLog))
      return false; 
    UserActionLog userActionLog = (UserActionLog)paramObject;
    Collections.sort(this.applicationIds);
    Collections.sort(userActionLog.applicationIds);
    return (Objects.equals(Boolean.valueOf(this.emailUserOnEnd), Boolean.valueOf(userActionLog.emailUserOnEnd)) && 
      Objects.equals(this.actioneeUserId, userActionLog.actioneeUserId) && 
      Objects.equals(this.actionerUserId, userActionLog.actionerUserId) && 
      Objects.equals(this.applicationIds, userActionLog.applicationIds) && 
      Objects.equals(this.comment, userActionLog.comment) && 
      Objects.equals(this.id, userActionLog.id) && 
      Objects.equals(this.insertInstant, userActionLog.insertInstant) && 
      Objects.equals(this.endEventSent, userActionLog.endEventSent) && 
      Objects.equals(Boolean.valueOf(this.notifyUserOnEnd), Boolean.valueOf(userActionLog.notifyUserOnEnd)) && 
      Objects.equals(this.expiry, userActionLog.expiry) && 
      Objects.equals(this.history, userActionLog.history) && 
      Objects.equals(this.localizedName, userActionLog.localizedName) && 
      Objects.equals(this.localizedOption, userActionLog.localizedOption) && 
      Objects.equals(this.localizedReason, userActionLog.localizedReason) && 
      Objects.equals(this.name, userActionLog.name) && 
      Objects.equals(this.option, userActionLog.option) && 
      Objects.equals(this.reason, userActionLog.reason) && 
      Objects.equals(this.reasonCode, userActionLog.reasonCode) && 
      Objects.equals(this.userActionId, userActionLog.userActionId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { 
          this.actioneeUserId, this.actionerUserId, this.applicationIds, this.comment, this.id, this.insertInstant, Boolean.valueOf(this.emailUserOnEnd), this.endEventSent, this.expiry, this.history, 
          this.localizedName, this.localizedOption, this.localizedReason, this.name, this.option, this.reason, this.reasonCode, this.userActionId, 
          Boolean.valueOf(this.notifyUserOnEnd) });
  }
  
  @JsonIgnore
  public boolean isActive() {
    return (this.expiry != null && this.expiry.isAfter(ZonedDateTime.now(ZoneOffset.UTC)));
  }
  
  public void normalize() {
    this.comment = Normalizer.trim(this.comment);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
