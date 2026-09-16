package io.fusionauth.app.action.api.system;

import com.google.inject.Inject;
import io.fusionauth.api.service.system.EventLogService;
import io.fusionauth.app.action.api.BaseAPIAction;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.EventLogResponse;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(value = "{id}", requiresAuthentication = true, scheme = {"api-no-tenant"})
public class EventLogAction extends BaseAPIAction {
  private final EventLogService eventLogService;
  
  public Integer id;
  
  @JSONResponse
  public EventLogResponse response;
  
  @Inject
  public EventLogAction(FrontEndSupport paramFrontEndSupport, EventLogService paramEventLogService) {
    super(paramFrontEndSupport);
    this.eventLogService = paramEventLogService;
  }
  
  public String get() {
    this.response = new EventLogResponse(this.eventLogService.retrieveById(this.id.intValue()));
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.id == null)
      this.frontEndSupport.addGeneralError("[invalid]", new Object[0]); 
  }
}
