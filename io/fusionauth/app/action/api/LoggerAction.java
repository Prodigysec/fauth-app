package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.logging.LoggingService;
import io.fusionauth.app.service.FrontEndSupport;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class LoggerAction extends BaseAPIAction {
  private final LoggingService loggingService;
  
  public LoggingService.LoggingLevel level = null;
  
  public String name = null;
  
  @JSONResponse
  public Response response = new Response();
  
  @Inject
  public LoggerAction(FrontEndSupport paramFrontEndSupport, LoggingService paramLoggingService) {
    super(paramFrontEndSupport);
    this.loggingService = paramLoggingService;
  }
  
  public String get() {
    if (this.level != null)
      this.loggingService.setLevel(this.name, this.level); 
    this.response = new Response(this.loggingService.getLevel(this.name));
    return "render";
  }
  
  public String post() {
    this.loggingService.setLevel(this.name, this.level);
    this.response = new Response(this.loggingService.getLevel(this.name));
    return "render";
  }
  
  @ValidationMethod(httpMethods = {"GET"})
  public void validateGet() {
    if (this.name == null)
      this.frontEndSupport.addFieldError("name", "[blank]name", new Object[0]); 
  }
  
  @ValidationMethod
  public void validatePost() {
    if (this.level == null)
      this.frontEndSupport.addFieldError("level", "[blank]level", new Object[0]); 
    if (this.name == null)
      this.frontEndSupport.addFieldError("name", "[blank]name", new Object[0]); 
  }
  
  public static class Response {
    public LoggingService.LoggingLevel level;
    
    public Response() {}
    
    public Response(LoggingService.LoggingLevel param1LoggingLevel) {
      this.level = param1LoggingLevel;
    }
  }
}
