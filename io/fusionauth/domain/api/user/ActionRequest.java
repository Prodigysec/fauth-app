package io.fusionauth.domain.api.user;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseEventRequest;
import io.fusionauth.domain.util.Normalizer;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ActionRequest extends BaseEventRequest {
  public ActionData action;
  
  public boolean broadcast;
  
  @JacksonConstructor
  public ActionRequest() {}
  
  public ActionRequest(ActionData paramActionData, boolean paramBoolean) {
    this.action = paramActionData;
    this.broadcast = paramBoolean;
  }
  
  public ActionRequest(EventInfo paramEventInfo, ActionData paramActionData, boolean paramBoolean) {
    super(paramEventInfo);
    this.action = paramActionData;
    this.broadcast = paramBoolean;
  }
  
  public void normalize() {
    if (this.action != null)
      this.action.normalize(); 
  }
  
  public static class ActionData implements Buildable<ActionData> {
    public UUID actioneeUserId;
    
    public UUID actionerUserId;
    
    public List<UUID> applicationIds;
    
    public String comment;
    
    public boolean emailUser;
    
    public ZonedDateTime expiry;
    
    public boolean notifyUser;
    
    public String option;
    
    public UUID reasonId;
    
    public UUID userActionId;
    
    public ActionData() {}
    
    public ActionData(UUID param1UUID1, UUID param1UUID2, UUID param1UUID3, String param1String1, ZonedDateTime param1ZonedDateTime, boolean param1Boolean1, boolean param1Boolean2, String param1String2, UUID param1UUID4, UUID... param1VarArgs) {
      this.userActionId = param1UUID1;
      this.actioneeUserId = param1UUID2;
      this.actionerUserId = param1UUID3;
      this.comment = param1String1;
      this.expiry = param1ZonedDateTime;
      this.notifyUser = param1Boolean1;
      this.emailUser = param1Boolean2;
      this.option = param1String2;
      this.reasonId = param1UUID4;
      normalize();
      Collections.addAll(this.applicationIds, param1VarArgs);
    }
    
    public void normalize() {
      this.comment = Normalizer.trim(this.comment);
      if (this.applicationIds == null) {
        this.applicationIds = new ArrayList<>();
      } else {
        Normalizer.removeEmpty(this.applicationIds);
      } 
    }
  }
}
