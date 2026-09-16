package io.fusionauth.app.action.api;

import com.google.inject.Inject;
import io.fusionauth.api.service.reactor.ActivateResult;
import io.fusionauth.api.service.reactor.ReactorService;
import io.fusionauth.app.service.FrontEndSupport;
import io.fusionauth.domain.api.ReactorRequest;
import io.fusionauth.domain.api.ReactorResponse;
import io.fusionauth.domain.reactor.ReactorFeatureStatus;
import io.fusionauth.domain.reactor.ReactorStatus;
import org.primeframework.mvc.action.annotation.Action;
import org.primeframework.mvc.content.json.annotation.JSONRequest;
import org.primeframework.mvc.content.json.annotation.JSONResponse;
import org.primeframework.mvc.validation.ValidationMethod;

@Action(requiresAuthentication = true, scheme = {"api-no-tenant"})
public class ReactorAction extends BaseAPIAction {
  @JSONRequest
  public final ReactorRequest request = new ReactorRequest();
  
  private final ReactorService reactorService;
  
  @JSONResponse
  public ReactorResponse response;
  
  @Inject
  public ReactorAction(FrontEndSupport paramFrontEndSupport, ReactorService paramReactorService) {
    super(paramFrontEndSupport);
    this.reactorService = paramReactorService;
  }
  
  public String delete() {
    this.reactorService.deactivate();
    return "success";
  }
  
  public String get() {
    this.response = new ReactorResponse();
    this.response.status = this.frontEndSupport.getReactorStatus();
    return "render";
  }
  
  public String post() {
    if ((this.frontEndSupport.getReactorStatus()).licensed) {
      this.frontEndSupport.addGeneralError("[alreadyLicensed]", new Object[0]);
      return "conflict";
    } 
    ActivateResult activateResult = this.reactorService.activate(this.request.licenseId, this.request.license);
    if (!activateResult.success()) {
      this.frontEndSupport.addFieldError("licenseId", "[invalid]licenseId", new Object[0]);
      return "error";
    } 
    return "success";
  }
  
  public String put() {
    if (this.reactorService.regenerate())
      return "success"; 
    return "error-status";
  }
  
  @ValidationMethod(httpMethods = {"POST"})
  public void validatePost() {
    if (this.request.licenseId == null) {
      this.frontEndSupport.addFieldError("licenseId", "[missing]licenseId", new Object[0]);
    } else if (this.request.licenseId.length() > 255) {
      this.frontEndSupport.addFieldError("licenseId", "[invalid]licenseId", new Object[0]);
    } 
  }
  
  @ValidationMethod(httpMethods = {"PUT"})
  public void validatePut() {
    ReactorStatus reactorStatus = this.frontEndSupport.getReactorStatus();
    if (reactorStatus.breachedPasswordDetection != ReactorFeatureStatus.ACTIVE && reactorStatus.breachedPasswordDetection != ReactorFeatureStatus.DISCONNECTED)
      this.frontEndSupport.addGeneralError("[RegenerationNotAllowed]", new Object[0]); 
  }
}
