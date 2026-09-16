package io.fusionauth.domain;

import com.inversoft.json.ToString;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.UUID;

public class UserComment implements Buildable<UserComment> {
  public String comment;
  
  public UUID commenterId;
  
  public UUID id;
  
  public ZonedDateTime insertInstant;
  
  public UUID userId;
  
  public UserComment() {}
  
  public UserComment(String paramString, ZonedDateTime paramZonedDateTime, UUID paramUUID1, UUID paramUUID2, UUID paramUUID3) {
    this.comment = paramString;
    this.insertInstant = paramZonedDateTime;
    this.id = paramUUID1;
    this.userId = paramUUID2;
    this.commenterId = paramUUID3;
  }
  
  public static UserActionLog toUserActionLog(UserComment paramUserComment) {
    return (new UserActionLog()).with(paramUserActionLog -> paramUserActionLog.actioneeUserId = paramUserComment.userId)
      .with(paramUserActionLog -> paramUserActionLog.actionerUserId = paramUserComment.commenterId)
      .with(paramUserActionLog -> paramUserActionLog.comment = paramUserComment.comment)
      .with(paramUserActionLog -> paramUserActionLog.insertInstant = paramUserComment.insertInstant);
  }
  
  public boolean equals(Object paramObject) {
    if (this == paramObject)
      return true; 
    if (!(paramObject instanceof UserComment))
      return false; 
    UserComment userComment = (UserComment)paramObject;
    return (Objects.equals(this.comment, userComment.comment) && 
      Objects.equals(this.commenterId, userComment.commenterId) && 
      Objects.equals(this.id, userComment.id) && 
      Objects.equals(this.insertInstant, userComment.insertInstant) && 
      Objects.equals(this.userId, userComment.userId));
  }
  
  public int hashCode() {
    return Objects.hash(new Object[] { this.comment, this.commenterId, this.id, this.insertInstant, this.userId });
  }
  
  public void normalize() {
    this.comment = Normalizer.trim(this.comment);
  }
  
  public String toString() {
    return ToString.toString(this);
  }
}
