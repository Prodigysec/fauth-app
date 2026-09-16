package io.fusionauth.api.service.moderation.cleanspeak;

import java.util.List;

public class ApplicationResponse {
  public Application application;
  
  public List<Application> applications;
  
  public ApplicationResponse() {}
  
  public ApplicationResponse(List<Application> paramList) {
    this.applications = paramList;
    this.applications.sort(Application::compareTo);
  }
  
  public ApplicationResponse(Application paramApplication) {
    this.application = paramApplication;
  }
}
