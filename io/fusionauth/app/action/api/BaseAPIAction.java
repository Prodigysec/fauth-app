package io.fusionauth.app.action.api;

import com.inversoft.error.Errors;
import io.fusionauth.api.domain.api.RequestContext;
import io.fusionauth.api.util.NetworkTools;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.EventInfo;
import io.fusionauth.domain.api.BaseEventRequest;
import io.fusionauth.domain.api.BaseLoginRequest;
import org.primeframework.mvc.action.ActionInvocation;
import org.primeframework.mvc.action.result.annotation.JSON;
import org.primeframework.mvc.action.result.annotation.JSON.List;
import org.primeframework.mvc.action.result.annotation.Status;
import org.primeframework.mvc.action.result.annotation.Status.List;
import org.primeframework.mvc.content.ValidContentTypes;
import org.primeframework.mvc.content.json.JacksonActionConfiguration;
import org.primeframework.mvc.parameter.annotation.PostParameterMethod;

@List({@JSON(code = "render", status = 200, cacheControl = "no-store"), @JSON(code = "accepted", status = 202, cacheControl = "no-store"), @JSON(code = "input", status = 400, cacheControl = "no-store"), @JSON(code = "username-unavailable", status = 400, cacheControl = "no-store"), @JSON(code = "inactive", status = 403, cacheControl = "no-store"), @JSON(code = "conflict", status = 409, cacheControl = "no-store"), @JSON(code = "rate-limited", status = 429, cacheControl = "no-store"), @JSON(code = "error", status = 500, cacheControl = "no-store"), @JSON(code = "search-exception", status = 503, cacheControl = "no-store"), @JSON(code = "webhook-transaction-failed", status = 504, cacheControl = "no-store"), @JSON(code = "lambda-invocation-error", status = 512, cacheControl = "no-store")})
@List({@Status(code = "success", status = 200), @Status(code = "accepted-status", status = 202), @Status(code = "no-content", status = 204), @Status(code = "unauthenticated", status = 401), @Status(code = "unauthorized", status = 401), @Status(code = "disabled", status = 403), @Status(code = "email-disabled", status = 403), @Status(code = "forbidden", status = 403), @Status(code = "missing", status = 404), @Status(code = "not-allowed", status = 405), @Status(code = "content-length-required", status = 411), @Status(code = "bad-parameter", status = 420), @Status(code = "invalid-two-factor-code", status = 421), @Status(code = "user-locked", status = 423), @Status(code = "error-status", status = 501), @Status(code = "not-implemented", status = 501), @Status(code = "service-unavailable", status = 503)})
@ValidContentTypes({"application/json"})
public abstract class BaseAPIAction {
  protected final FrontEndSupport frontEndSupport;
  
  protected BaseAPIAction(FrontEndSupport paramFrontEndSupport) {
    this.frontEndSupport = paramFrontEndSupport;
    RequestContext.set(new RequestContext(paramFrontEndSupport.request.getPath()));
  }
  
  @PostParameterMethod
  public void resolveEventInfo() {
    ActionInvocation actionInvocation = this.frontEndSupport.actionInvocationStore.getCurrent();
    JacksonActionConfiguration jacksonActionConfiguration = (JacksonActionConfiguration)actionInvocation.configuration.additionalConfiguration.get(JacksonActionConfiguration.class);
    if (jacksonActionConfiguration == null)
      return; 
    JacksonActionConfiguration.RequestMember requestMember = (JacksonActionConfiguration.RequestMember)jacksonActionConfiguration.requestMembers.get(this.frontEndSupport.method);
    if (requestMember == null)
      return; 
    Object object = this.frontEndSupport.expressionEvaluator.getValue(requestMember.name, this);
    if (object == null)
      return; 
    if (object instanceof BaseEventRequest) {
      BaseEventRequest baseEventRequest = (BaseEventRequest)object;
      if (baseEventRequest instanceof BaseLoginRequest) {
        BaseLoginRequest baseLoginRequest = (BaseLoginRequest)baseEventRequest;
        baseLoginRequest.normalize();
      } 
      if (baseEventRequest.eventInfo == null)
        baseEventRequest.eventInfo = new EventInfo(); 
      if (baseEventRequest.eventInfo.ipAddress == null) {
        baseEventRequest.eventInfo.ipAddress = this.frontEndSupport.getTrustedClientIPAddress();
      } else {
        baseEventRequest.eventInfo.ipAddress = NetworkTools.sanitizeIPAddress(baseEventRequest.eventInfo.ipAddress);
      } 
    } 
  }
  
  protected String conflict() {
    this.frontEndSupport.transfer((new Errors()).addGeneralError("[conflict]", "The request could not be completed because of a conflict.", new Object[0]));
    return "conflict";
  }
  
  protected String retryableConflict() {
    this.frontEndSupport.transfer((new Errors()).addGeneralError("[retryableConflict]", "The request could not be completed because of a conflict. Please use an exponential backoff and try again.", new Object[0]));
    return "conflict";
  }
}
