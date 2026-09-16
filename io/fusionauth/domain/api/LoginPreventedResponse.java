package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Buildable;
import io.fusionauth.domain.UserActionLog;
import java.time.ZonedDateTime;
import java.util.UUID;

public class LoginPreventedResponse implements Buildable<LoginPreventedResponse> {
  public UUID actionId;
  
  public UUID actionerUserId;
  
  public ZonedDateTime expiry;
  
  public String localizedName;
  
  public String localizedOption;
  
  public String localizedReason;
  
  public String name;
  
  public String option;
  
  public String reason;
  
  public String reasonCode;
  
  public LoginPreventedResponse(UserActionLog paramUserActionLog) {
    this.actionId = paramUserActionLog.userActionId;
    this.actionerUserId = paramUserActionLog.actionerUserId;
    this.expiry = paramUserActionLog.expiry;
    this.localizedName = paramUserActionLog.localizedName;
    this.localizedReason = paramUserActionLog.localizedReason;
    this.name = paramUserActionLog.name;
    this.reason = paramUserActionLog.reason;
    this.reasonCode = paramUserActionLog.reasonCode;
  }
  
  @JacksonConstructor
  public LoginPreventedResponse() {}
}
