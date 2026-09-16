package io.fusionauth.domain.api;

import com.inversoft.json.JacksonConstructor;
import io.fusionauth.domain.Application;
import io.fusionauth.domain.ApplicationRole;
import java.util.List;

public class ApplicationResponse {
  public Application application;
  
  public List<Application> applications;
  
  public ApplicationRole role;
  
  @JacksonConstructor
  public ApplicationResponse() {}
  
  public ApplicationResponse(Application paramApplication) {
    this.application = paramApplication;
  }
  
  public ApplicationResponse(List<Application> paramList) {
    this.applications = paramList;
  }
  
  public ApplicationResponse(ApplicationRole paramApplicationRole) {
    this.role = paramApplicationRole;
  }
}
