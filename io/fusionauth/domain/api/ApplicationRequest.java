package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import io.fusionauth.domain.EventInfo;
import java.util.UUID;

public class ApplicationRequest extends BaseEventRequest {
  public Application application;
  
  public ApplicationRole role;
  
  public UUID sourceApplicationId;
  
  @JacksonConstructor
  public ApplicationRequest() {}
  
  public ApplicationRequest(Application paramApplication) {
    this.application = paramApplication;
  }
  
  public ApplicationRequest(ApplicationRole paramApplicationRole) {
    this.role = paramApplicationRole;
  }
  
  public ApplicationRequest(EventInfo paramEventInfo, Application paramApplication) {
    super(paramEventInfo);
    this.application = paramApplication;
  }
  
  public ApplicationRequest(EventInfo paramEventInfo, ApplicationRole paramApplicationRole) {
    super(paramEventInfo);
    this.role = paramApplicationRole;
  }
}
